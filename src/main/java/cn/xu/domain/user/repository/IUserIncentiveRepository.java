package cn.xu.domain.user.repository;

import cn.xu.domain.user.model.entity.UserBadgeEntity;
import cn.xu.domain.user.model.entity.UserLevelEntity;
import cn.xu.domain.user.model.entity.UserPointEntity;

import java.util.List;

/**
 * 用户激励仓储接口
 * 遵循DDD原则，只处理用户激励领域实体的操作
 */
public interface IUserIncentiveRepository {
    
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
     * 根据用户ID查询勋章列表
     */
    List<UserBadgeEntity> findUserBadgesByUserId(Long userId);
    
    /**
     * 保存用户勋章记录
     */
    void saveUserBadge(UserBadgeEntity userBadgeEntity);
    
    /**
     * 检查用户是否拥有指定勋章
     */
    boolean existsUserBadge(Long userId, Long badgeId);
    
    /**
     * 查询积分排行榜
     */
    List<UserPointEntity> findPointsRanking(int limit);
    
    /**
     * 查询等级排行榜
     */
    List<UserLevelEntity> findLevelRanking(int limit);
}