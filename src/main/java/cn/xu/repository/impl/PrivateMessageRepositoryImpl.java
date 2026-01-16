package cn.xu.repository.impl;

import cn.xu.model.entity.PrivateMessage;
import cn.xu.repository.PrivateMessageRepository;
import cn.xu.repository.mapper.PrivateMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 私信消息仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PrivateMessageRepositoryImpl implements PrivateMessageRepository {
    
    private final PrivateMessageMapper mapper;
    
    @Override
    public Long save(PrivateMessage message) {
        mapper.insert(message);
        return message.getId();
    }
    
    @Override
    public void update(PrivateMessage message) {
        mapper.update(message);
    }
    
    @Override
    public Optional<PrivateMessage> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }
    
    @Override
    public List<PrivateMessage> findMessagesBetweenUsers(Long userId1, Long userId2, Long viewerId, int offset, int limit) {
        return mapper.selectMessagesBetweenUsers(userId1, userId2, viewerId, offset, limit);
    }
    
    @Override
    public int countUnread(Long receiverId, Long senderId) {
        return mapper.countUnread(receiverId, senderId);
    }
    
    @Override
    public void markAsRead(Long receiverId, Long senderId) {
        mapper.markAsRead(receiverId, senderId);
    }
    
    @Override
    public void withdraw(Long messageId, Long senderId) {
        mapper.withdraw(messageId, senderId);
    }
    
    @Override
    public boolean hasMessageFrom(Long senderId, Long receiverId) {
        return mapper.hasMessageFrom(senderId, receiverId);
    }
    
    @Override
    public boolean hasReplyFrom(Long senderId, Long receiverId) {
        return mapper.hasReplyFrom(senderId, receiverId);
    }
    
    @Override
    public void deleteBySender(Long messageId, Long senderId) {
        mapper.deleteBySender(messageId, senderId);
    }
    
    @Override
    public void deleteByReceiver(Long messageId, Long receiverId) {
        mapper.deleteByReceiver(messageId, receiverId);
    }
    
    @Override
    public void softDeleteForUser(Long messageId, Long userId) {
        // 查询消息，判断用户是发送方还是接收方
        PrivateMessage message = mapper.selectById(messageId);
        if (message != null) {
            if (userId.equals(message.getSenderId())) {
                mapper.deleteBySender(messageId, userId);
            } else if (userId.equals(message.getReceiverId())) {
                mapper.deleteByReceiver(messageId, userId);
            }
        }
    }
    
    @Override
    public void updatePendingToDelivered(Long senderId, Long receiverId) {
        mapper.updatePendingToDelivered(senderId, receiverId);
    }
    
    @Override
    public List<PrivateMessage> searchMessages(Long userId, String keyword, int offset, int limit) {
        return mapper.searchMessages(userId, keyword, offset, limit);
    }
}
