package cn.xu.domain.user.repository;

import cn.xu.domain.user.model.entity.UserLevelEntity;
import cn.xu.domain.user.model.entity.UserPointEntity;

import java.util.List;

/**
 * 用户激励仓储接口（简化版，去除勋章系统）
 * 遵循DDD原则，只处理用户激励领域实体的操作
 */
public interface IUserIncentiveRepositorySimplified {
    
    /**
     * 根据用户ID查询积分记录
     */
    UserPointEntity findUserPointsByUserId(Long userId);
    
    /**
     * 保存用户积分记录
     */
    void saveUserPoints(UserPointEntity userPointEntity);
    
    /**
     * 根据用户ID查询等级记录
     */
    UserLevelEntity findUserLevelByUserId(Long userId);
    
    /**
     * 保存用户等级记录
     */
    void saveUserLevel(UserLevelEntity userLevelEntity);
    
    /**
     * 查询积分排行榜
     */
    List<UserPointEntity> findPointsRanking(int limit);
    
    /**
     * 查询等级排行榜
     */
    List<UserLevelEntity> findLevelRanking(int limit);
}