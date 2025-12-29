package cn.xu.service.message;

import cn.xu.event.publisher.MessageEventPublisher;
import cn.xu.model.entity.PrivateMessage;
import cn.xu.model.entity.User;
import cn.xu.model.entity.UserConversation;
import cn.xu.model.vo.message.ConversationListVO;
import cn.xu.repository.GreetingRecordRepository;
import cn.xu.repository.PrivateMessageRepository;
import cn.xu.repository.UserBlockRepository;
import cn.xu.repository.UserConversationRepository;
import cn.xu.service.follow.FollowService;
import cn.xu.service.user.UserService;
import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 私信服务
 *
 * <p>权限规则:
 * <ul>
 *   <li>互关 - 完全开放</li>
 *   <li>对方回复过 - 完全开放</li>
 *   <li>陌生人/单向关注 - 可发1条打招呼消息</li>
 *   <li>拉黑 - 不可发</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageService {

    private final UserConversationRepository conversationRepository;
    private final PrivateMessageRepository messageRepository;
    private final UserBlockRepository userBlockRepository;
    private final GreetingRecordRepository greetingRecordRepository;
    private final UserService userService;
    private final FollowService followService;
    private final MessageEventPublisher eventPublisher;

    private static final int MAX_CONTENT_LENGTH = 1000;
    private static final int MAX_PREVIEW_LENGTH = 100;

    // ==================== 权限检查 ====================

    /**
     * 检查私信发送权限
     */
    public PermissionResult canSendDM(Long senderId, Long receiverId) {
        log.info("[权限检查] sender:{} → receiver:{}", senderId, receiverId);
        
        // 1. 拉黑检查
        if (userBlockRepository.existsBlock(senderId, receiverId)) {
            return PermissionResult.denied("你已屏蔽对方，无法发送消息");
        }
        if (userBlockRepository.existsBlock(receiverId, senderId)) {
            return PermissionResult.denied("对方设置了消息权限，暂时无法发送");
        }

        // 2. 互关检查
        boolean iFollow = followService.isFollowed(senderId, receiverId);
        boolean theyFollow = followService.isFollowed(receiverId, senderId);
        if (iFollow && theyFollow) {
            log.info("[权限检查] 互关，允许发送");
            return PermissionResult.allowed();
        }

        // 3. 对方回复过检查
        if (messageRepository.hasReplyFrom(senderId, receiverId)) {
            log.info("[权限检查] 对方已回复，允许发送");
            return PermissionResult.allowed();
        }

        // 4. 打招呼消息检查
        if (greetingRecordRepository.hasSentGreeting(senderId, receiverId)) {
            return PermissionResult.denied("已发送消息，等待对方回复后可继续发送");
        }

        // 5. 允许发送打招呼消息
        log.info("[权限检查] 允许发送打招呼消息");
        return PermissionResult.allowedAsGreeting();
    }

    // ==================== 消息发送 ====================

    /**
     * 发送私信
     */
    @Transactional(rollbackFor = Exception.class)
    public SendResult sendMessage(Long senderId, Long receiverId, String content) {
        log.info("[发送私信] ========== 开始 ==========");
        log.info("[发送私信] sender:{} → receiver:{}, content长度:{}", senderId, receiverId, content != null ? content.length() : 0);

        // 1. 参数校验
        validateSendRequest(senderId, receiverId, content);

        // 2. 权限校验
        PermissionResult permission = canSendDM(senderId, receiverId);
        if (!permission.isAllowed()) {
            log.warn("[发送私信] 权限不足: {}", permission.getReason());
            throw new BusinessException(permission.getReason());
        }

        // 3. 创建消息
        PrivateMessage message = createMessage(senderId, receiverId, content);
        Long messageId = messageRepository.save(message);
        log.info("[发送私信] 消息已保存 messageId:{}", messageId);

        // 4. 更新双方会话（关键步骤）
        String preview = getPreview(content);
        updateBothConversations(senderId, receiverId, preview);

        // 5. 处理打招呼记录
        handleGreetingRecord(senderId, receiverId, permission.isGreeting());

        // 6. 发布事件（WebSocket推送）
        publishMessageSentEvent(senderId, receiverId, messageId, preview, permission.isGreeting());

        log.info("[发送私信] ========== 完成 messageId:{} ==========", messageId);
        return new SendResult(messageId, PrivateMessage.STATUS_DELIVERED, permission.isGreeting() ? "打招呼消息" : "");
    }

    /**
     * 创建消息实体
     */
    private PrivateMessage createMessage(Long senderId, Long receiverId, String content) {
        // 检测是否为图片消息
        if (isImageMessage(content)) {
            String imageUrl = extractImageUrl(content);
            PrivateMessage msg = PrivateMessage.createImage(senderId, receiverId, imageUrl, PrivateMessage.STATUS_DELIVERED);
            msg.setContent(content);
            return msg;
        }
        return PrivateMessage.createText(senderId, receiverId, content, PrivateMessage.STATUS_DELIVERED);
    }

    /**
     * 更新双方会话记录（核心方法）
     */
    private void updateBothConversations(Long senderId, Long receiverId, String preview) {
        log.info("[会话更新] 开始更新双方会话");
        
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(receiverId);
        
        if (sender == null || receiver == null) {
            log.error("[会话更新] 用户不存在 sender:{} receiver:{}", sender, receiver);
            throw new BusinessException("用户不存在");
        }
        
        boolean isMutual = followService.isFollowed(senderId, receiverId) 
                       && followService.isFollowed(receiverId, senderId);
        
        // 更新发送者的会话
        updateSingleConversation(senderId, receiverId, receiver.getNickname(), receiver.getAvatar(), 
                                 preview, true, isMutual);
        log.info("[会话更新] 发送者会话已更新 owner:{} other:{}", senderId, receiverId);
        
        // 更新接收者的会话（关键！）
        updateSingleConversation(receiverId, senderId, sender.getNickname(), sender.getAvatar(), 
                                 preview, false, isMutual);
        log.info("[会话更新] 接收者会话已更新 owner:{} other:{}", receiverId, senderId);
    }

    /**
     * 更新单个用户的会话记录
     */
    private void updateSingleConversation(Long ownerId, Long otherId, String otherNickname, 
                                          String otherAvatar, String preview, boolean isSender, boolean isMutual) {
        Optional<UserConversation> existingOpt = conversationRepository.findByOwnerAndOther(ownerId, otherId);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (existingOpt.isPresent()) {
            // 更新现有会话
            UserConversation conv = existingOpt.get();
            conv.setOtherNickname(otherNickname);
            conv.setOtherAvatar(otherAvatar);
            conv.setLastMessage(preview);
            conv.setLastMessageTime(now);
            conv.setLastMessageIsMine(isSender ? 1 : 0);
            conv.setIsDeleted(0);
            conv.setUpdateTime(now);
            if (isMutual) {
                conv.setRelationType(UserConversation.RELATION_MUTUAL);
            }
            conversationRepository.update(conv);
            log.info("[会话更新] 更新现有会话 convId:{} owner:{} other:{}", conv.getId(), ownerId, otherId);
            
            // 接收者增加未读数
            if (!isSender) {
                conversationRepository.incrementUnreadCount(ownerId, otherId);
                log.info("[会话更新] 接收者未读数+1 owner:{}", ownerId);
            }
        } else {
            // 创建新会话
            UserConversation conv = new UserConversation();
            conv.setOwnerId(ownerId);
            conv.setOtherUserId(otherId);
            conv.setOtherNickname(otherNickname);
            conv.setOtherAvatar(otherAvatar);
            conv.setRelationType(isMutual ? UserConversation.RELATION_MUTUAL : UserConversation.RELATION_STRANGER);
            conv.setConversationStatus(UserConversation.STATUS_ESTABLISHED);
            conv.setIsInitiator(isSender ? 1 : 0);
            conv.setIsBlocked(0);
            conv.setIsBlockedBy(0);
            conv.setUnreadCount(isSender ? 0 : 1);
            conv.setLastMessage(preview);
            conv.setLastMessageTime(now);
            conv.setLastMessageIsMine(isSender ? 1 : 0);
            conv.setIsPinned(0);
            conv.setIsMuted(0);
            conv.setIsDeleted(0);
            conv.setCreateTime(now);
            conv.setUpdateTime(now);
            
            conversationRepository.save(conv);
            log.info("[会话更新] 创建新会话 convId:{} owner:{} other:{} unread:{}", 
                    conv.getId(), ownerId, otherId, conv.getUnreadCount());
        }
    }

    /**
     * 处理打招呼记录
     */
    private void handleGreetingRecord(Long senderId, Long receiverId, boolean isGreeting) {
        if (isGreeting) {
            greetingRecordRepository.markGreetingSent(senderId, receiverId);
            log.info("[打招呼] 记录已保存 sender:{} → receiver:{}", senderId, receiverId);
        }
        
        // 如果对方之前发过打招呼消息，现在回复了，清理记录
        if (greetingRecordRepository.hasSentGreeting(receiverId, senderId)) {
            greetingRecordRepository.deleteGreeting(receiverId, senderId);
            log.info("[打招呼] 清理对方的打招呼记录 {} → {}", receiverId, senderId);
        }
    }

    /**
     * 发布消息发送事件（事务提交后执行）
     */
    private void publishMessageSentEvent(Long senderId, Long receiverId, Long messageId, 
                                         String content, boolean isGreeting) {
        // 注册事务同步，确保事务提交后再发布事件
        final MessageEventPublisher publisher = this.eventPublisher;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    publisher.publishSent(senderId, receiverId, messageId, content, isGreeting);
                } catch (Exception e) {
                    log.error("[事件发布] 消息发送事件发布失败 messageId:{}", messageId, e);
                }
            }
        });
        log.info("[事件发布] 已注册事务同步 messageId:{}", messageId);
    }

    // ==================== 会话查询 ====================

    /**
     * 获取会话列表
     */
    public List<ConversationListVO> getConversationList(Long userId, int page, int size) {
        int offset = Math.max(0, (page - 1) * size);
        log.info("[会话列表] 查询 userId:{} page:{} size:{}", userId, page, size);
        
        List<UserConversation> conversations = conversationRepository.findActiveByOwnerId(userId, offset, size);
        log.info("[会话列表] 查询到 {} 条会话", conversations.size());
        
        return conversations.stream()
                .map(this::toConversationVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取或创建会话
     */
    @Transactional(rollbackFor = Exception.class)
    public ConversationListVO getOrCreateConversation(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException("不能和自己创建会话");
        }
        
        Optional<UserConversation> existingOpt = conversationRepository.findByOwnerAndOther(currentUserId, targetUserId);
        if (existingOpt.isPresent()) {
            return toConversationVO(existingOpt.get());
        }
        
        // 创建新会话
        User targetUser = userService.getUserById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        boolean isMutual = followService.isFollowed(currentUserId, targetUserId) 
                       && followService.isFollowed(targetUserId, currentUserId);
        
        LocalDateTime now = LocalDateTime.now();
        UserConversation conv = new UserConversation();
        conv.setOwnerId(currentUserId);
        conv.setOtherUserId(targetUserId);
        conv.setOtherNickname(targetUser.getNickname());
        conv.setOtherAvatar(targetUser.getAvatar());
        conv.setRelationType(isMutual ? UserConversation.RELATION_MUTUAL : UserConversation.RELATION_STRANGER);
        conv.setConversationStatus(UserConversation.STATUS_ESTABLISHED);
        conv.setIsInitiator(1);
        conv.setIsBlocked(0);
        conv.setIsBlockedBy(0);
        conv.setUnreadCount(0);
        conv.setLastMessageTime(now);
        conv.setLastMessageIsMine(1);
        conv.setIsPinned(0);
        conv.setIsMuted(0);
        conv.setIsDeleted(0);
        conv.setCreateTime(now);
        conv.setUpdateTime(now);
        
        conversationRepository.save(conv);
        log.info("[会话创建] 新会话 convId:{} owner:{} other:{}", conv.getId(), currentUserId, targetUserId);
        
        return toConversationVO(conv);
    }

    /**
     * 删除会话
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long currentUserId, Long otherUserId) {
        conversationRepository.softDelete(currentUserId, otherUserId);
        log.info("[会话删除] owner:{} other:{}", currentUserId, otherUserId);
    }

    // ==================== 消息查询 ====================

    /**
     * 获取消息列表
     */
    public List<PrivateMessage> getMessages(Long currentUserId, Long otherUserId, int page, int size) {
        int offset = Math.max(0, (page - 1) * size);
        log.info("[消息列表] 查询 user:{} other:{} page:{}", currentUserId, otherUserId, page);
        return messageRepository.findMessagesBetweenUsers(currentUserId, otherUserId, currentUserId, offset, size);
    }

    /**
     * 标记消息已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long currentUserId, Long otherUserId) {
        messageRepository.markAsRead(currentUserId, otherUserId);
        conversationRepository.clearUnreadCount(currentUserId, otherUserId);
        log.info("[标记已读] user:{} other:{}", currentUserId, otherUserId);
        
        // 发布已读事件
        try {
            eventPublisher.publishRead(currentUserId, otherUserId);
        } catch (Exception e) {
            log.error("[事件发布] 已读事件发布失败: {}", e.getMessage());
        }
    }

    /**
     * 删除消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long currentUserId, Long messageId) {
        messageRepository.softDeleteForUser(messageId, currentUserId);
        log.info("[消息删除] user:{} messageId:{}", currentUserId, messageId);
    }

    // ==================== 统计 ====================

    public int getTotalUnreadCount(Long userId) {
        return conversationRepository.getTotalUnreadCount(userId);
    }

    public int getUnreadCount(Long currentUserId, Long otherUserId) {
        return conversationRepository.getUnreadCount(currentUserId, otherUserId);
    }

    // ==================== 工具方法 ====================

    private void validateSendRequest(Long senderId, Long receiverId, String content) {
        if (senderId == null || receiverId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (senderId.equals(receiverId)) {
            throw new BusinessException("不能给自己发私信");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("消息内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException("消息内容超过" + MAX_CONTENT_LENGTH + "字限制");
        }
    }

    private String getPreview(String content) {
        if (content == null) return "";
        if (isImageMessage(content)) return "[图片]";
        return content.length() > MAX_PREVIEW_LENGTH ? content.substring(0, MAX_PREVIEW_LENGTH) : content;
    }

    private boolean isImageMessage(String content) {
        if (content == null || !content.trim().startsWith("{")) return false;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(content);
            return node.has("type") && "image".equals(node.get("type").asText()) && node.has("url");
        } catch (Exception e) {
            return false;
        }
    }

    private String extractImageUrl(String content) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(content);
            return node.get("url").asText();
        } catch (Exception e) {
            return "";
        }
    }

    private ConversationListVO toConversationVO(UserConversation conv) {
        ConversationListVO vo = new ConversationListVO();
        vo.setId(conv.getId());
        vo.setUserId(conv.getOtherUserId());
        vo.setUserName(conv.getOtherNickname());
        vo.setUserAvatar(conv.getOtherAvatar());
        vo.setLastMessage(conv.getLastMessage());
        vo.setLastMessageTime(conv.getLastMessageTime());
        vo.setUnreadCount(conv.getUnreadCount() != null ? conv.getUnreadCount() : 0);
        vo.setRelationType(conv.getRelationType());
        return vo;
    }

    // ==================== 结果类 ====================

    @Getter
    @AllArgsConstructor
    public static class SendResult {
        private final Long messageId;
        private final int status;
        private final String type;
    }

    @Getter
    @AllArgsConstructor
    public static class PermissionResult {
        private final boolean allowed;
        private final boolean greeting;
        private final String reason;

        public static PermissionResult allowed() {
            return new PermissionResult(true, false, "");
        }

        public static PermissionResult allowedAsGreeting() {
            return new PermissionResult(true, true, "");
        }

        public static PermissionResult denied(String reason) {
            return new PermissionResult(false, false, reason);
        }

        public boolean isGreeting() { return greeting; }
        public boolean isAllowed() { return allowed; }
        public String getReason() { return reason; }
    }
}
