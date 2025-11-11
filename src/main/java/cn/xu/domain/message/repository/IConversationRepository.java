package cn.xu.domain.message.repository;

import cn.xu.domain.message.model.entity.ConversationEntity;

import java.util.List;
import java.util.Optional;

/**
 * 对话关系仓储接口
 */
public interface IConversationRepository {
    
    /**
     * 保存对话关系
     */
    Long save(ConversationEntity conversation);
    
    /**
     * 更新对话关系
     */
    void update(ConversationEntity conversation);
    
    /**
     * 根据ID查找对话关系
     */
    Optional<ConversationEntity> findById(Long id);
    
    /**
     * 根据两个用户ID查找对话关系
     */
    Optional<ConversationEntity> findByUserPair(Long userId1, Long userId2);
    
    /**
     * 检查对话关系是否存在
     */
    boolean existsConversation(Long userId1, Long userId2);
    
    /**
     * 查询用户的所有对话列表
     */
    List<ConversationEntity> findByUserId(Long userId, Integer offset, Integer limit);
    
    /**
     * 删除对话关系
     */
    void deleteById(Long id);
}

