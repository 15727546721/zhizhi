package cn.xu.repository;

import cn.xu.model.entity.UserBlock;

import java.util.List;
import java.util.Optional;

/**
 * 用户屏蔽仓储接口
 * 
 * 简化设计：直接使用PO，移除Entity转换
 */
public interface IUserBlockRepository {
    
    /**
     * 保存屏蔽关系
     */
    Long save(UserBlock userBlock);
    
    /**
     * 删除屏蔽关系
     */
    void deleteById(Long id);
    
    /**
     * 根据ID查找屏蔽关系
     */
    Optional<UserBlock> findById(Long id);
    
    /**
     * 检查用户是否屏蔽了另一个用户
     */
    boolean existsBlock(Long userId, Long blockedUserId);
    
    /**
     * 根据用户ID和被屏蔽用户ID查找屏蔽关系
     */
    Optional<UserBlock> findByUserAndBlockedUser(Long userId, Long blockedUserId);
    
    /**
     * 查询用户的屏蔽列表
     */
    List<UserBlock> findByUserId(Long userId, Integer offset, Integer limit);
    
    /**
     * 统计用户的屏蔽数量
     */
    int countByUserId(Long userId);
    
    /**
     * 根据用户ID和被屏蔽用户ID删除屏蔽关系
     */
    void deleteByUserIdAndBlockedUserId(Long userId, Long blockedUserId);
}

