package cn.xu.domain.message.repository;

import cn.xu.domain.message.model.entity.MessageEntity;
import cn.xu.domain.message.model.entity.MessageType;

import java.util.List;

public interface IMessageRepository {
    void save(MessageEntity message);
    
    MessageEntity findById(Long id);
    
    List<MessageEntity> findByUserId(Long userId, MessageType type, int offset, int limit);
    
    long countUnreadMessages(Long userId);
    
    void markAsRead(Long messageId);
    
    void markAllAsRead(Long userId);
    
    void deleteById(Long id);
    
    boolean exists(Long id);
} 