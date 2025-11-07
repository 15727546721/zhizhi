package cn.xu.domain.like.repository;

import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.model.aggregate.LikeAggregate;

import java.util.Optional;

/**
 * 点赞聚合根仓储接口
 * 遵循DDD原则，只处理聚合根的操作
 */
public interface ILikeAggregateRepository {
    
    /**
     * 保存点赞聚合根
     * @param aggregate 点赞聚合根
     * @return 聚合根ID
     */
    Long save(LikeAggregate aggregate);
    
    /**
     * 更新点赞聚合根
     * @param aggregate 点赞聚合根
     */
    void update(LikeAggregate aggregate);
    
    /**
     * 根据ID查找点赞聚合根
     * @param id 点赞ID
     * @return 点赞聚合根
     */
    Optional<LikeAggregate> findById(Long id);
    
    /**
     * 根据用户ID、类型和目标ID查找点赞聚合根
     * @param userId 用户ID
     * @param type 点赞类型
     * @param targetId 目标ID
     * @return 点赞聚合根
     */
    Optional<LikeAggregate> findByUserAndTarget(Long userId, LikeType type, Long targetId);
    
    /**
     * 删除点赞聚合根
     * @param id 点赞ID
     */
    void deleteById(Long id);
    
    /**
     * 检查点赞关系是否存在
     * @param userId 用户ID
     * @param type 点赞类型
     * @param targetId 目标ID
     * @return 是否存在
     */
    boolean existsByUserAndTarget(Long userId, LikeType type, Long targetId);
    
    /**
     * 获取目标的点赞数
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    long countByTarget(Long targetId, LikeType type);
    
    /**
     * 获取用户点赞的目标ID列表
     * @param userId 用户ID
     * @param type 点赞类型
     * @return 目标ID列表
     */
    java.util.List<Long> findLikedTargetIdsByUser(Long userId, LikeType type);
    
    /**
     * 获取点赞某个目标的用户ID列表
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 用户ID列表
     */
    java.util.List<Long> findUserIdsByTarget(Long targetId, LikeType type);
}