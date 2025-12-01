package cn.xu.repository.impl;

import cn.xu.model.entity.PrivateMessage;
import cn.xu.repository.IPrivateMessageRepository;
import cn.xu.repository.mapper.PrivateMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 私信仓储实现
 * 
 * @author xu
 * @since 2025-11-28
 */
@Repository
@RequiredArgsConstructor
public class PrivateMessageRepository implements IPrivateMessageRepository {
    
    private final PrivateMessageMapper privateMessageMapper;
    
    @Override
    public Long save(PrivateMessage message) {
        if (message.getId() == null) {
            privateMessageMapper.insert(message);
            return message.getId();
        } else {
            privateMessageMapper.update(message);
            return message.getId();
        }
    }
    
    @Override
    public void update(PrivateMessage message) {
        privateMessageMapper.update(message);
    }
    
    @Override
    public PrivateMessage findById(Long id) {
        return privateMessageMapper.selectById(id);
    }
    
    @Override
    public List<PrivateMessage> findMessagesBetweenUsers(Long userId1, Long userId2, int offset, int limit) {
        List<PrivateMessage> messages = privateMessageMapper.selectPrivateMessagesBetweenUsers(userId1, userId2, offset, limit);
        if (messages == null) {
            return new ArrayList<>();
        }
        // 反转列表，使最新的消息在最后（前端显示需要）
        Collections.reverse(messages);
        return messages;
    }
    
    @Override
    public List<PrivateMessage> findMessagesByReceiver(Long receiverId, Long senderId, int offset, int limit) {
        // 接收者只能看到status=1的消息
        List<PrivateMessage> messages = privateMessageMapper.selectPrivateMessagesByReceiver(
                receiverId, senderId, PrivateMessage.STATUS_DELIVERED, offset, limit);
        return messages != null ? messages : new ArrayList<>();
    }
    
    @Override
    public List<PrivateMessage> findMessagesBySender(Long senderId, Long receiverId, int offset, int limit) {
        List<PrivateMessage> messages = privateMessageMapper.selectPrivateMessagesBySender(senderId, receiverId, offset, limit);
        return messages != null ? messages : new ArrayList<>();
    }
    
    @Override
    public List<PrivateMessage> findLastMessagesByUser(Long userId, int offset, int limit) {
        List<PrivateMessage> messages = privateMessageMapper.selectLastPrivateMessagesByUser(userId, offset, limit);
        return messages != null ? messages : new ArrayList<>();
    }
    
    @Override
    public long countUnreadMessages(Long receiverId, Long senderId) {
        Long count = privateMessageMapper.countUnreadPrivateMessages(receiverId, senderId);
        return count != null ? count : 0L;
    }
    
    @Override
    public void markAsRead(Long messageId, Long receiverId) {
        privateMessageMapper.markAsRead(messageId);
    }
    
    @Override
    public void markAsReadBySender(Long receiverId, Long senderId) {
        privateMessageMapper.markPrivateMessagesAsRead(receiverId, senderId);
    }
    
    @Override
    public void updateStatusBySenderAndReceiver(Long senderId, Long receiverId, int oldStatus, int newStatus) {
        privateMessageMapper.updateMessageStatus(senderId, receiverId, oldStatus, newStatus);
    }
    
    // ========== 批量查询优化（解决N+1问题） ==========
    
    @Override
    public List<PrivateMessage> findLastMessagesBatch(Long currentUserId, List<Long> otherUserIds) {
        if (otherUserIds == null || otherUserIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<PrivateMessage> messages = privateMessageMapper.selectLastMessagesForConversations(currentUserId, otherUserIds);
        return messages != null ? messages : new ArrayList<>();
    }
    
    @Override
    public List<UnreadCountResult> countUnreadMessagesBatch(Long currentUserId, List<Long> otherUserIds) {
        if (otherUserIds == null || otherUserIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 调用Mapper的批量查询方法
        List<UnreadCountDto> dtos = privateMessageMapper.countUnreadMessagesForConversations(currentUserId, otherUserIds);
        if (dtos == null) {
            return new ArrayList<>();
        }
        
        // 转换为结果对象
        return dtos.stream()
                .map(dto -> new UnreadCountResult(dto.getOtherUserId(), dto.getUnreadCount()))
                .collect(Collectors.toList());
    }
    
    /**
     * Mapper返回的DTO对象
     */
    public static class UnreadCountDto {
        private Long otherUserId;
        private Long unreadCount;
        
        public UnreadCountDto() {}
        
        public UnreadCountDto(Long otherUserId, Long unreadCount) {
            this.otherUserId = otherUserId;
            this.unreadCount = unreadCount;
        }
        
        public Long getOtherUserId() { return otherUserId; }
        public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }
        public Long getUnreadCount() { return unreadCount; }
        public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
    }
}

