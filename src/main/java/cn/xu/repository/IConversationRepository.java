package cn.xu.repository;

import cn.xu.model.entity.Conversation;

import java.util.List;
import java.util.Optional;

/**
 * 对话关系仓储接口
 * 
 * <p>直接操作Conversation PO，避免Entity转换
 *
 * @author xu
 * @since 2025-11-26
 */
public interface IConversationRepository {
    
    /**
     * 保存对话关系
     */
    Long save(Conversation conversation);
    
    /**
     * 更新对话关系
     */
    void update(Conversation conversation);
    
    /**
     * 根据ID查找对话关系
     */
    Optional<Conversation> findById(Long id);
    
    /**
     * 根据两个用户ID查找对话关系
     */
    Optional<Conversation> findByUserPair(Long userId1, Long userId2);
    
    /**
     * 检查对话关系是否存在
     */
    boolean existsConversation(Long userId1, Long userId2);
    
    /**
     * 查询用户的所有对话列表
     */
    List<Conversation> findByUserId(Long userId, Integer offset, Integer limit);
    
    /**
     * 删除对话关系
     */
    void deleteById(Long id);
}

