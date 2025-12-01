package cn.xu.repository;

import cn.xu.model.entity.PrivateMessage;

import java.util.List;

/**
 * 私信仓储接口
 * 
 * @author xu
 * @since 2025-11-28
 */
public interface IPrivateMessageRepository {
    
    // ========== 基础CRUD ==========
    
    Long save(PrivateMessage message);
    
    void update(PrivateMessage message);
    
    PrivateMessage findById(Long id);
    
    // ========== 消息查询 ==========
    
    /** 查询两个用户之间的所有消息（合并发送和接收） */
    List<PrivateMessage> findMessagesBetweenUsers(Long userId1, Long userId2, int offset, int limit);
    
    /** 查询两个用户之间的消息（接收者视角，只查询status=1的消息） */
    List<PrivateMessage> findMessagesByReceiver(Long receiverId, Long senderId, int offset, int limit);
    
    /** 查询两个用户之间的消息（发送者视角，查询所有消息） */
    List<PrivateMessage> findMessagesBySender(Long senderId, Long receiverId, int offset, int limit);
    
    /** 查询用户的所有对话的最后一条消息 */
    List<PrivateMessage> findLastMessagesByUser(Long userId, int offset, int limit);
    
    // ========== 已读状态 ==========
    
    /** 统计未读消息数（只统计status=1的未读消息） */
    long countUnreadMessages(Long receiverId, Long senderId);
    
    /** 标记消息为已读 */
    void markAsRead(Long messageId, Long receiverId);
    
    /** 批量标记消息为已读 */
    void markAsReadBySender(Long receiverId, Long senderId);
    
    // ========== 状态更新 ==========
    
    /** 更新消息状态（将oldStatus的消息更新为newStatus） */
    void updateStatusBySenderAndReceiver(Long senderId, Long receiverId, int oldStatus, int newStatus);
    
    // ========== 批量查询优化（解决N+1问题） ==========
    
    /** 批量获取用户与多个对话伙伴的最后一条消息 */
    List<PrivateMessage> findLastMessagesBatch(Long currentUserId, List<Long> otherUserIds);
    
    /** 批量获取用户与多个对话伙伴的未读消息数 */
    List<UnreadCountResult> countUnreadMessagesBatch(Long currentUserId, List<Long> otherUserIds);
    
    /**
     * 未读消息统计结果
     */
    class UnreadCountResult {
        private Long otherUserId;
        private Long unreadCount;
        
        public UnreadCountResult(Long otherUserId, Long unreadCount) {
            this.otherUserId = otherUserId;
            this.unreadCount = unreadCount;
        }
        
        public Long getOtherUserId() { return otherUserId; }
        public Long getUnreadCount() { return unreadCount; }
    }
}

