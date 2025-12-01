package cn.xu.service.message;

import cn.xu.model.entity.Conversation;
import cn.xu.model.entity.PrivateMessage;
import cn.xu.model.entity.User;
import cn.xu.repository.IConversationRepository;
import cn.xu.repository.IPrivateMessageRepository;
import cn.xu.repository.IUserBlockRepository;
import cn.xu.service.follow.FollowService;
import cn.xu.service.user.IUserService;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.util.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

/**
 * 私信服务
 * 
 * <p>提供私信的发送、接收、管理等功能
 * <ul>
 * <li>发送私信</li>
 * <li>接收私信</li>
 * <li>管理会话</li>
 * </ul>
 *
 * @author xu  
 * @since 2025-11-26
 */
@Service
@Slf4j
public class PrivateMessageService {

    private final IPrivateMessageRepository privateMessageRepository;
    private final IConversationRepository conversationRepository; 
    private final IUserBlockRepository userBlockRepository;
    private final IUserService userService;
    private final FollowService followService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /** 消息限流器，防止用户频繁发送消息 */
    private RateLimiter messageLimiter;
    
    /** 每分钟最大消息数 */
    private static final int MAX_MESSAGES_PER_MINUTE = 10;
    private static final int RATE_LIMIT_WINDOW_SECONDS = 60;
    
    public PrivateMessageService(
            IPrivateMessageRepository privateMessageRepository,
            IConversationRepository conversationRepository,
            IUserBlockRepository userBlockRepository,
            IUserService userService,
            FollowService followService,
            RedisTemplate<String, Object> redisTemplate) {
        this.privateMessageRepository = privateMessageRepository;
        this.conversationRepository = conversationRepository;
        this.userBlockRepository = userBlockRepository;
        this.userService = userService;
        this.followService = followService;
        this.redisTemplate = redisTemplate;
    }
    
    @PostConstruct
    public void init() {
        // 初始化限流器
        this.messageLimiter = new RateLimiter(
                redisTemplate,
                "rate:private_message",
                MAX_MESSAGES_PER_MINUTE,
                RATE_LIMIT_WINDOW_SECONDS
        );
        log.info("[私信服务] 初始化完成 - 限流配置: {}条/{}秒", MAX_MESSAGES_PER_MINUTE, RATE_LIMIT_WINDOW_SECONDS);
    }

    // ==================== 发送消息 ====================

    /**
     * 发送私信消息
     * 
     * <p>消息状态说明:
     * <ul>
     * <li>status=1 已发送</li>
     * <li>status=1 待审核</li>
     * <li>status=2 已删除</li>
     * <li>status=1 已读</li>
     * </ul>
     */
    @Transactional(rollbackFor = Exception.class)
    public SendResult sendMessage(Long senderId, Long receiverId, String content) {
        log.info("[私信服务] 发送消息 - 发送者: {}, 接收者: {}", senderId, receiverId);

        // 0. 限流检查
        if (messageLimiter != null && !messageLimiter.allowRequest(String.valueOf(senderId))) {
            log.warn("[私信服务] 发送频率超限 - 用户: {}", senderId);
            throw new BusinessException("发送过于频繁，请稍后再试");
        }

        // 1. 参数校验
        validateSendRequest(senderId, receiverId, content);

        // 2. 检查是否被拉黑
        if (userBlockRepository.existsBlock(receiverId, senderId)) {
            log.warn("[私信服务] 用户被拉黑 - 发送者: {}, 接收者: {}", senderId, receiverId);
            throw new BusinessException("消息发送失败");
        }

        // 3. 检查是否互相关注
        boolean isMutualFollow = followService.isFollowed(senderId, receiverId) 
                && followService.isFollowed(receiverId, senderId);

        if (isMutualFollow) {
            return sendMutualFollowMessage(senderId, receiverId, content);
        } else {
            return sendNonMutualFollowMessage(senderId, receiverId, content);
        }
    }

    /**
     * 发送结果
     */
    public static class SendResult {
        private final Long messageId;
        private final String message;
        private final int status;

        public SendResult(Long messageId, String message, int status) {
            this.messageId = messageId;
            this.message = message;
            this.status = status;
        }

        public Long getMessageId() { return messageId; }
        public String getMessage() { return message; }
        public int getStatus() { return status; }
    }

    /**
     * 发送互相关注用户的消息
     */
    private SendResult sendMutualFollowMessage(Long senderId, Long receiverId, String content) {
        // 互相关注，直接发送
        PrivateMessage message = PrivateMessage.createDelivered(senderId, receiverId, content);
        Long messageId = privateMessageRepository.save(message);

        // 更新或创建会话
        updateOrCreateConversation(senderId, receiverId, senderId, Conversation.STATUS_ESTABLISHED);

        log.info("[私信服务] 互关消息发送成功 - messageId: {}", messageId);
        return new SendResult(messageId, "", PrivateMessage.STATUS_DELIVERED);
    }

    /**
     * 发送非互相关注用户的消息
     */
    private SendResult sendNonMutualFollowMessage(Long senderId, Long receiverId, String content) {
        // 查询会话
        Optional<Conversation> conversationOpt = getConversation(senderId, receiverId);

        if (!conversationOpt.isPresent()) {
            // 首次发送
            return handleFirstMessage(senderId, receiverId, content);
        }

        Conversation conversation = conversationOpt.get();
        
        if (conversation.isPending()) {
            // 会话待确认
            if (conversation.isInitiator(senderId)) {
                // 发起者继续发送，进入防骚扰
                return handleAntiSpamMessage(senderId, receiverId, content);
            } else {
                // 接收者回复，建立会话
                return handleReplyMessage(senderId, receiverId, content, conversation);
            }
        } else {
            // 会话已建立
            return handleEstablishedMessage(senderId, receiverId, content, conversation);
        }
    }

    /**
     * 处理首次发送消息
     */
    private SendResult handleFirstMessage(Long senderId, Long receiverId, String content) {
        log.info("[私信服务] 首次消息 - 发送者: {}, 接收者: {}", senderId, receiverId);

        // 直接发送
        PrivateMessage message = PrivateMessage.createDelivered(senderId, receiverId, content);
        Long messageId = privateMessageRepository.save(message);

        // 创建待确认会话
        updateOrCreateConversation(senderId, receiverId, senderId, Conversation.STATUS_PENDING);

        return new SendResult(messageId, "", PrivateMessage.STATUS_DELIVERED);
    }

    /**
     * 处理防骚扰消息
     */
    private SendResult handleAntiSpamMessage(Long senderId, Long receiverId, String content) {
        log.warn("[私信服务] 防骚扰消息 - 发送者: {}, 接收者: {}", senderId, receiverId);

        // 消息进入待审核
        PrivateMessage message = PrivateMessage.createPending(senderId, receiverId, content);
        Long messageId = privateMessageRepository.save(message);

        // 更新会话时间
        updateConversationTime(senderId, receiverId);

        return new SendResult(messageId, "", PrivateMessage.STATUS_PENDING);
    }

    /**
     * 处理回复消息（建立会话）
     */
    private SendResult handleReplyMessage(Long senderId, Long receiverId, String content, Conversation conversation) {
        log.info("[私信服务] 回复消息 - 发送者: {}, 接收者: {}", senderId, receiverId);

        // 直接发送
        PrivateMessage message = PrivateMessage.createDelivered(senderId, receiverId, content);
        Long messageId = privateMessageRepository.save(message);

        // 建立会话
        conversation.establish();
        conversationRepository.update(conversation);

        return new SendResult(messageId, "", PrivateMessage.STATUS_DELIVERED);
    }

    /**
     * 处理已建立会话的消息
     */
    private SendResult handleEstablishedMessage(Long senderId, Long receiverId, String content, Conversation conversation) {
        // 直接发送
        PrivateMessage message = PrivateMessage.createDelivered(senderId, receiverId, content);
        Long messageId = privateMessageRepository.save(message);

        // 更新会话时间
        conversation.updateLastMessageTime();
        conversationRepository.update(conversation);

        return new SendResult(messageId, "", PrivateMessage.STATUS_DELIVERED);
    }

    // ==================== 查询消息 ====================

    /**
     * 获取两个用户之间的消息列表
     */
    public List<PrivateMessage> getMessagesBetweenUsers(Long currentUserId, Long otherUserId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return privateMessageRepository.findMessagesBetweenUsers(currentUserId, otherUserId, offset, pageSize);
    }

    /**
     * 获取用户的会话列表
     */
    public List<Conversation> getConversationList(Long userId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return conversationRepository.findByUserId(userId, offset, pageSize);
    }

    // ==================== 已读状态 ====================

    /**
     * 标记消息为已读
     */
    public void markAsRead(Long receiverId, Long senderId) {
        privateMessageRepository.markAsReadBySender(receiverId, senderId);
        log.debug("[私信服务] 标记已读 - 接收者: {}, 发送者: {}", receiverId, senderId);
    }

    /**
     * 获取未读消息数
     */
    public long getUnreadCount(Long receiverId, Long senderId) {
        return privateMessageRepository.countUnreadMessages(receiverId, senderId);
    }

    /**
     * 批量获取未读消息数（避免N+1查询）
     * 
     * @param currentUserId 当前用户ID
     * @param otherUserIds 其他用户ID列表
     * @return Map<用户ID, 未读数>
     */
    public java.util.Map<Long, Long> getUnreadCountBatch(Long currentUserId, java.util.List<Long> otherUserIds) {
        if (otherUserIds == null || otherUserIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        
        java.util.List<IPrivateMessageRepository.UnreadCountResult> results = 
                privateMessageRepository.countUnreadMessagesBatch(currentUserId, otherUserIds);
        
        java.util.Map<Long, Long> resultMap = new java.util.HashMap<>();
        for (IPrivateMessageRepository.UnreadCountResult result : results) {
            resultMap.put(result.getOtherUserId(), result.getUnreadCount());
        }
        
        // 补充默认值
        for (Long userId : otherUserIds) {
            resultMap.putIfAbsent(userId, 0L);
        }
        
        return resultMap;
    }

    // ==================== 私有方法 ====================

    /**
     * 验证发送请求参数
     */
    private void validateSendRequest(Long senderId, Long receiverId, String content) {
        if (senderId == null || receiverId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (senderId.equals(receiverId)) {
            throw new BusinessException("不能给自己发送私信");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("消息内容不能为空");
        }
        if (content.length() > 1000) {
            throw new BusinessException("消息内容不能超过1000字");
        }

        // 验证接收者是否存在
        User receiver = userService.getUserById(receiverId);
        if (receiver == null) {
            throw new BusinessException("接收用户不存在");
        }
    }

    /**
     * 
     */
    private Optional<Conversation> getConversation(Long userId1, Long userId2) {
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);
        return conversationRepository.findByUserPair(smallerId, largerId);
    }

    /**
     * ?
     */
    private void updateOrCreateConversation(Long userId1, Long userId2, Long creatorId, int status) {
        Optional<Conversation> existing = getConversation(userId1, userId2);
        if (existing.isPresent()) {
            Conversation conversation = existing.get();
            conversation.updateLastMessageTime();
            conversationRepository.update(conversation);
        } else {
            Conversation conversation;
            if (status == Conversation.STATUS_ESTABLISHED) {
                conversation = Conversation.createEstablished(userId1, userId2, creatorId);
            } else {
                conversation = Conversation.createPending(userId1, userId2, creatorId);
            }
            conversationRepository.save(conversation);
        }
    }

    /**
     * 
     */
    private void updateConversationTime(Long userId1, Long userId2) {
        Optional<Conversation> conversationOpt = getConversation(userId1, userId2);
        if (conversationOpt.isPresent()) {
            Conversation conversation = conversationOpt.get();
            conversation.updateLastMessageTime();
            conversationRepository.update(conversation);
        }
    }
}
