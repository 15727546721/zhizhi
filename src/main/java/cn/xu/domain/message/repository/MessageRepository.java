package cn.xu.domain.message.repository;

import cn.xu.domain.message.model.Message;
import java.util.List;

/**
 * 消息仓储接口
 */
public interface MessageRepository {
    
    /**
     * 保存消息
     */
    void save(Message message);
    
    /**
     * 获取用户的最近消息列表
     */
    List<Message> findRecentMessages(Long userId, int limit);
    
    /**
     * 获取两个用户之间的历史消息
     */
    List<Message> findMessageHistory(Long userId, Long friendId, Long lastMessageId, int limit);
    
    /**
     * 将与指定用户的消息标记为已读
     */
    void markAsRead(Long userId, Long friendId);
    
    /**
     * 获取与指定用户的未读消息数
     */
    int getUnreadCount(Long userId, Long friendId);
} 