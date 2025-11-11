package cn.xu.domain.message.repository;

import cn.xu.domain.message.model.entity.FirstMessageEntity;

import java.util.Optional;

/**
 * 首次消息记录仓储接口
 */
public interface IFirstMessageRepository {
    
    /**
     * 保存首次消息记录
     */
    Long save(FirstMessageEntity firstMessage);
    
    /**
     * 更新首次消息记录
     */
    void update(FirstMessageEntity firstMessage);
    
    /**
     * 根据发送者和接收者查找首次消息记录
     */
    Optional<FirstMessageEntity> findBySenderAndReceiver(Long senderId, Long receiverId);
    
    /**
     * 更新回复状态
     */
    void updateHasReplied(Long senderId, Long receiverId, boolean hasReplied);
    
    /**
     * 检查是否存在首次消息记录
     */
    boolean existsBySenderAndReceiver(Long senderId, Long receiverId);
}

