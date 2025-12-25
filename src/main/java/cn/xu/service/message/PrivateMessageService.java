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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 私信服务
 *
 * <p>权限规则:
 * <ul>
 *   <li>互关 - 完全开放</li>
 *   <li>对方回复过 - 完全开放（自由会话）</li>
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

    /** 消息内容最大长度 */
    private static final int MAX_CONTENT_LENGTH = 1000;
    /** 消息预览最大长度 */
    private static final int MAX_PREVIEW_LENGTH = 100;

    // ==================== 权限检查 ====================

    /**
     * 检查私信发送权限
     *
     * <p>权限规则（按优先级）：
     * <ul>
     *   <li>1. 拉黑 → 不可发送</li>
     *   <li>2. 互关 → 完全开放</li>
     *   <li>3. 对方回复过 → 完全开放（自由会话）</li>
     *   <li>4. 已发送打招呼消息 → 等待对方回复</li>
     *   <li>5. 陌生人/单向关注 → 允许发送1条打招呼消息</li>
     * </ul>
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @return 权限检查结果
     */
    public PermissionResult canSendDM(Long senderId, Long receiverId) {
        // 1. 拉黑检查 → 不可发送（区分提示语）
        if (userBlockRepository.existsBlock(senderId, receiverId)) {
            // 我屏蔽了对方
            return PermissionResult.denied("你已屏蔽对方，无法发送消息");
        }
        if (userBlockRepository.existsBlock(receiverId, senderId)) {
            // 对方屏蔽了我（隐私保护，用模糊说法）
            return PermissionResult.denied("对方设置了消息权限，暂时无法发送");
        }

        // 2. 互关检查 → 完全开放
        if (isMutualFollow(senderId, receiverId)) {
            log.debug("[权限] 互关，允许发送 sender:{} receiver:{}", senderId, receiverId);
            return PermissionResult.allowed();
        }

        // 3. 对方回复过 → 完全开放（自由会话）
        boolean hasReply = messageRepository.hasReplyFrom(senderId, receiverId);
        if (hasReply) {
            log.debug("[权限] 对方已回复，允许发送 sender:{} receiver:{}", senderId, receiverId);
            return PermissionResult.allowed();
        }

        // 4. 打招呼消息检查 → 已发送过则等待回复
        if (greetingRecordRepository.hasSentGreeting(senderId, receiverId)) {
            return PermissionResult.denied("已发送消息，等待对方回复后可继续发送");
        }

        // 5. 陌生人/单向关注 → 允许发送1条打招呼消息
        log.debug("[权限] 允许发送打招呼消息 sender:{} receiver:{}", senderId, receiverId);
        return PermissionResult.allowedAsGreeting();
    }

    /** 检查是否互相关注 */
    private boolean isMutualFollow(Long userId1, Long userId2) {
        return followService.isFollowed(userId1, userId2) 
            && followService.isFollowed(userId2, userId1);
    }

    // ==================== 消息发送 ====================

    /**
     * 发送私信（自动识别文本/图片）
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param content    消息内容（支持JSON格式图片: {"type":"image","url":"..."}）
     * @return 发送结果
     */
    @Transactional(rollbackFor = Exception.class)
    public SendResult sendMessage(Long senderId, Long receiverId, String content) {
        log.info("[发送私信] sender:{} → receiver:{}", senderId, receiverId);

        // 参数校验
        validateSendRequest(senderId, receiverId, content);

        // 权限校验
        PermissionResult permission = canSendDM(senderId, receiverId);
        if (!permission.isAllowed()) {
            log.warn("[发送私信] 权限不足 sender:{} reason:{}", senderId, permission.getReason());
            throw new BusinessException(permission.getReason());
        }

        // 检测是否为JSON格式的图片消息
        PrivateMessage message;
        String previewContent;
        if (isJsonImageMessage(content)) {
            // 图片消息：解析URL并创建图片消息
            String imageUrl = extractImageUrl(content);
            message = PrivateMessage.createImage(senderId, receiverId, imageUrl, PrivateMessage.STATUS_DELIVERED);
            message.setContent(content); // 保存完整JSON便于前端解析
            previewContent = "[图片]";
            log.info("[发送私信] 识别为图片消息");
        } else {
            // 文本消息
            message = PrivateMessage.createText(senderId, receiverId, content, PrivateMessage.STATUS_DELIVERED);
            previewContent = content;
        }
        
        Long messageId = messageRepository.save(message);

        // 更新会话
        updateConversations(senderId, receiverId, previewContent);

        // 记录打招呼消息
        if (permission.isGreeting()) {
            greetingRecordRepository.markGreetingSent(senderId, receiverId);
            log.info("[发送私信] 打招呼消息 sender:{} → receiver:{}", senderId, receiverId);
        }
        
        // 清理打招呼记录：如果对方之前发过打招呼消息，现在回复了，应该删除该记录
        // 这样双方就进入自由会话状态
        if (greetingRecordRepository.hasSentGreeting(receiverId, senderId)) {
            greetingRecordRepository.deleteGreeting(receiverId, senderId);
            log.info("[发送私信] 对方打招呼消息已确认，清理记录 receiver:{} → sender:{}", receiverId, senderId);
        }

        // 异步通知
        publishMessageEvent(senderId, receiverId, messageId, previewContent, permission.isGreeting());

        log.info("[发送私信] 成功 messageId:{}", messageId);
        return new SendResult(messageId, PrivateMessage.STATUS_DELIVERED, permission.isGreeting() ? "打招呼消息" : "");
    }
    
    /** 检测是否为JSON格式的图片消息 */
    private boolean isJsonImageMessage(String content) {
        if (content == null || !content.trim().startsWith("{")) {
            return false;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(content);
            return node.has("type") && "image".equals(node.get("type").asText()) && node.has("url");
        } catch (Exception e) {
            return false;
        }
    }
    
    /** 从JSON中提取图片URL */
    private String extractImageUrl(String content) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(content);
            return node.get("url").asText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 发送图片私信
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param imageUrl   图片URL
     * @return 发送结果
     */
    @Transactional(rollbackFor = Exception.class)
    public SendResult sendImage(Long senderId, Long receiverId, String imageUrl) {
        log.info("[发送图片] sender:{} → receiver:{}", senderId, receiverId);

        // 权限校验
        PermissionResult permission = canSendDM(senderId, receiverId);
        if (!permission.isAllowed()) {
            throw new BusinessException(permission.getReason());
        }

        // 保存消息
        PrivateMessage message = PrivateMessage.createImage(senderId, receiverId, imageUrl, PrivateMessage.STATUS_DELIVERED);
        Long messageId = messageRepository.save(message);

        // 更新会话
        updateConversations(senderId, receiverId, "[图片]");

        // 记录打招呼消息
        if (permission.isGreeting()) {
            greetingRecordRepository.markGreetingSent(senderId, receiverId);
        }

        return new SendResult(messageId, PrivateMessage.STATUS_DELIVERED, permission.isGreeting() ? "打招呼消息" : "");
    }

    /** 发布消息事件（事务提交后） */
    private void publishMessageEvent(Long senderId, Long receiverId, Long messageId, String content, boolean isGreeting) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishSent(senderId, receiverId, messageId, content, isGreeting);
            }
        });
    }

    // ==================== 会话更新 ====================

    /** 更新双方会话记录 */
    private void updateConversations(Long senderId, Long receiverId, String content) {
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(receiverId);
        boolean isMutual = isMutualFollow(senderId, receiverId);
        String preview = truncate(content, MAX_PREVIEW_LENGTH);

        log.debug("[会话更新] senderId:{} senderNickname:{}, receiverId:{} receiverNickname:{}", 
                senderId, sender.getNickname(), receiverId, receiver.getNickname());

        // 更新发送者会话：owner=发送者，other=接收者
        updateOrCreateConversation(senderId, receiverId, receiver, preview, true, isMutual);
        // 更新接收者会话：owner=接收者，other=发送者
        updateOrCreateConversation(receiverId, senderId, sender, preview, false, isMutual);
    }

    /** 更新或创建单方会话 */
    private void updateOrCreateConversation(Long ownerId, Long otherId, User otherUser, 
                                            String preview, boolean isSender, boolean isMutual) {
        Optional<UserConversation> convOpt = conversationRepository.findByOwnerAndOther(ownerId, otherId);
        
        if (convOpt.isPresent()) {
            UserConversation conv = convOpt.get();
            // 同步更新对方用户信息（防止用户改名后会话显示旧名）
            conv.setOtherNickname(otherUser.getNickname());
            conv.setOtherAvatar(otherUser.getAvatar());
            conv.setLastMessage(preview);
            conv.setLastMessageTime(java.time.LocalDateTime.now());
            conv.setLastMessageIsMine(isSender ? 1 : 0);
            conv.setIsDeleted(0); // 恢复已删除的会话
            if (isMutual) {
                conv.setRelationType(UserConversation.RELATION_MUTUAL);
            }
            log.debug("[会话更新] 更新现有会话 ownerId:{} otherId:{} isSender:{}", 
                    ownerId, otherId, isSender);
            conversationRepository.update(conv);
            
            // 接收者：原子性增加未读数（避免并发问题）
            if (!isSender) {
                conversationRepository.incrementUnreadCount(ownerId, otherId);
                log.debug("[会话更新] 接收者未读数+1 ownerId:{} otherId:{}", ownerId, otherId);
            }
        } else {
            UserConversation conv = isSender
                    ? UserConversation.createForSender(ownerId, otherId, otherUser.getNickname(), otherUser.getAvatar(), isMutual)
                    : UserConversation.createForReceiver(ownerId, otherId, otherUser.getNickname(), otherUser.getAvatar(), isMutual);
            conv.setLastMessage(preview);
            log.debug("[会话创建] 新建会话 ownerId:{} otherId:{} isSender:{}", 
                    ownerId, otherId, isSender);
            conversationRepository.save(conv);
        }
    }

    // ==================== 参数校验 ====================

    /** 校验发送请求 */
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

    /** 截断字符串 */
    private String truncate(String str, int maxLength) {
        return str != null && str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    // ==================== 会话查询 ====================

    /** 获取会话列表 */
    public List<ConversationListVO> getConversationList(Long userId, int page, int size) {
        int offset = Math.max(0, (page - 1) * size);
        List<UserConversation> conversations = conversationRepository.findActiveByOwnerId(userId, offset, size);
        
        // 打印原始数据便于调试
        conversations.forEach(conv -> log.info("[会话列表-原始] id:{} ownerId:{} otherUserId:{} otherNickname:{}", 
                conv.getId(), conv.getOwnerId(), conv.getOtherUserId(), conv.getOtherNickname()));
        
        return conversations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /** 获取或创建会话 */
    @Transactional(rollbackFor = Exception.class)
    public ConversationListVO getOrCreateConversation(Long currentUserId, Long targetUserId) {
        // 不能和自己创建会话
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException("不能和自己创建会话");
        }
        return conversationRepository.findByOwnerAndOther(currentUserId, targetUserId)
                .map(this::convertToVO)
                .orElseGet(() -> createNewConversation(currentUserId, targetUserId));
    }

    /** 创建新会话 */
    private ConversationListVO createNewConversation(Long currentUserId, Long targetUserId) {
        User targetUser = userService.getUserById(targetUserId);
        boolean isMutual = isMutualFollow(currentUserId, targetUserId);
        
        log.debug("[会话创建] currentUserId:{} targetUserId:{} targetNickname:{}", 
                currentUserId, targetUserId, targetUser.getNickname());
        
        UserConversation conv = UserConversation.createForSender(
                currentUserId, targetUserId,
                targetUser.getNickname(), targetUser.getAvatar(), isMutual);
        conversationRepository.save(conv);
        
        return convertToVO(conv);
    }

    /** 删除会话（软删除） */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long currentUserId, Long otherUserId) {
        conversationRepository.softDelete(currentUserId, otherUserId);
    }

    /** 获取消息列表 */
    public List<PrivateMessage> getMessages(Long currentUserId, Long otherUserId, int page, int size) {
        int offset = Math.max(0, (page - 1) * size);
        return messageRepository.findMessagesBetweenUsers(currentUserId, otherUserId, currentUserId, offset, size);
    }

    // ==================== 消息状态 ====================

    /** 标记消息已读 */
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long currentUserId, Long otherUserId) {
        messageRepository.markAsRead(currentUserId, otherUserId);
        conversationRepository.clearUnreadCount(currentUserId, otherUserId);
        
        // 发布已读事件，通知对方
        eventPublisher.publishRead(currentUserId, otherUserId);
    }

    /** 删除消息（仅自己不可见） */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long currentUserId, Long messageId) {
        messageRepository.softDeleteForUser(messageId, currentUserId);
    }

    /** 获取总未读数 */
    public int getTotalUnreadCount(Long userId) {
        return conversationRepository.getTotalUnreadCount(userId);
    }

    /** 获取与某用户的未读数 */
    public int getUnreadCount(Long currentUserId, Long otherUserId) {
        return conversationRepository.getUnreadCount(currentUserId, otherUserId);
    }

    // ==================== 数据转换 ====================

    /** 转换会话实体为VO */
    private ConversationListVO convertToVO(UserConversation conv) {
        ConversationListVO vo = new ConversationListVO();
        vo.setId(conv.getId());
        vo.setUserId(conv.getOtherUserId());
        vo.setUserName(conv.getOtherNickname());
        vo.setUserAvatar(conv.getOtherAvatar());
        vo.setLastMessage(conv.getLastMessage());
        vo.setLastMessageTime(conv.getLastMessageTime());
        vo.setUnreadCount(conv.getUnreadCount());
        vo.setRelationType(conv.getRelationType());
        return vo;
    }

    // ==================== 结果类 ====================

    /** 消息发送结果 */
    @Getter
    @AllArgsConstructor
    public static class SendResult {
        private final Long messageId;
        private final int status;
        private final String type;
    }

    /**
     * 权限检查结果
     *
     * <p>三种状态：
     * <ul>
     *   <li>allowed=true, greeting=false → 完全开放（互关/对方已回复）</li>
     *   <li>allowed=true, greeting=true → 允许发送1条打招呼消息（陌生人/单向关注）</li>
     *   <li>allowed=false → 拒绝发送（已发送打招呼/拉黑）</li>
     * </ul>
     */
    @Getter
    @AllArgsConstructor
    public static class PermissionResult {
        /** 是否允许发送 */
        private final boolean allowed;
        /** 是否为打招呼消息（仅能发送1条） */
        private final boolean greeting;
        /** 拒绝原因（allowed=false时有值） */
        private final String reason;

        /** 完全开放（互关/对方已回复） */
        public static PermissionResult allowed() {
            return new PermissionResult(true, false, "");
        }

        /** 允许发送1条打招呼消息（陌生人/单向关注） */
        public static PermissionResult allowedAsGreeting() {
            return new PermissionResult(true, true, "");
        }

        /** 拒绝发送 */
        public static PermissionResult denied(String reason) {
            return new PermissionResult(false, false, reason);
        }

        public boolean isGreeting() { return greeting; }
        public boolean isAllowed() { return allowed; }
        public String getReason() { return reason; }
    }
}
