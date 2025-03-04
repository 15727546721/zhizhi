package cn.xu.domain.message.repository;

import cn.xu.domain.message.model.PrivateMessage;

import java.util.List;

/**
 * 私信仓储接口
 */
public interface PrivateMessageRepository {
    
    /**
     * 保存私信
     */
    void save(PrivateMessage message);
    
    /**
     * 获取用户的最近私信列表
     */
    List<PrivateMessage> findRecentMessages(Long userId, int limit);
    
    /**
     * 获取两个用户之间的历史私信
     */
    List<PrivateMessage> findMessageHistory(Long userId, Long friendId, Long lastMessageId, int limit);
    
    /**
     * 将与指定用户的私信标记为已读
     */
    void markAsRead(Long userId, Long friendId);
    
    /**
     * 获取与指定用户的未读消息数
     */
    int getUnreadCount(Long userId, Long friendId);
} 