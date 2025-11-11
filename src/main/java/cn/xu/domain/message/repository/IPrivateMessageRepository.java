package cn.xu.domain.message.repository;

import cn.xu.domain.message.model.aggregate.PrivateMessageAggregate;
import cn.xu.domain.message.model.valueobject.MessageStatus;

import java.util.List;
import java.util.Optional;

/**
 * 私信聚合根仓储接口
 * 遵循DDD原则，只处理聚合根的操作
 */
public interface IPrivateMessageRepository {
    
    /**
     * 保存私信聚合根
     */
    Long save(PrivateMessageAggregate aggregate);
    
    /**
     * 更新私信聚合根
     */
    void update(PrivateMessageAggregate aggregate);
    
    /**
     * 根据ID查找私信聚合根
     */
    Optional<PrivateMessageAggregate> findById(Long id);
    
    /**
     * 查询两个用户之间的消息（接收者视角，只查询status=1的消息）
     */
    List<PrivateMessageAggregate> findMessagesByReceiver(Long receiverId, Long senderId, Integer offset, Integer limit);
    
    /**
     * 查询两个用户之间的消息（发送者视角，查询所有消息）
     */
    List<PrivateMessageAggregate> findMessagesBySender(Long senderId, Long receiverId, Integer offset, Integer limit);
    
    /**
     * 查询两个用户之间的所有消息（合并发送和接收）
     */
    List<PrivateMessageAggregate> findMessagesBetweenUsers(Long userId1, Long userId2, Integer offset, Integer limit);
    
    /**
     * 统计未读消息数（只统计status=1的未读消息）
     */
    long countUnreadMessages(Long receiverId, Long senderId);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId, Long receiverId);
    
    /**
     * 批量标记消息为已读
     */
    void markAsReadBySender(Long receiverId, Long senderId);
    
    /**
     * 更新消息状态（将status=2的消息更新为status=1）
     */
    void updateStatusBySenderAndReceiver(Long senderId, Long receiverId, MessageStatus oldStatus, MessageStatus newStatus);
    
    /**
     * 查询用户的所有对话的最后一条消息
     */
    List<PrivateMessageAggregate> findLastMessagesByUser(Long userId, Integer offset, Integer limit);
}

