package cn.xu.domain.user.service;

import cn.xu.domain.user.model.entity.UserBadgeEntity;
import cn.xu.domain.user.model.entity.UserLevelEntity;
import cn.xu.domain.user.model.entity.UserPointEntity;

import java.util.List;

/**
 * 用户激励领域服务接口
 * 处理用户积分、等级、勋章等激励相关的业务逻辑
 */
public interface IUserIncentiveService {
    
    /**
     * 获取用户积分信息
     *
     * @param userId 用户ID
     * @return 用户积分实体
     */
    UserPointEntity getUserPoints(Long userId);
    
    /**
     * 增加用户积分
     *
     * @param userId  用户ID
     * @param points  积分数量
     * @param reason  积分增加原因
     */
    void addUserPoints(Long userId, Long points, String reason);
    
    /**
     * 消费用户积分
     *
     * @param userId  用户ID
     * @param points  积分数量
     * @param reason  积分消费原因
     */
    void consumeUserPoints(Long userId, Long points, String reason);
    
    /**
     * 获取用户等级信息
     *
     * @param userId 用户ID
     * @return 用户等级实体
     */
    UserLevelEntity getUserLevel(Long userId);
    
    /**
     * 增加用户经验值
     *
     * @param userId  用户ID
     * @param exp     经验值数量
     * @param reason  经验值增加原因
     */
    void addUserExp(Long userId, Long exp, String reason);
    
    /**
     * 获取用户勋章列表
     *
     * @param userId 用户ID
     * @return 用户勋章列表
     */
    List<UserBadgeEntity> getUserBadges(Long userId);
    
    /**
     * 授予用户勋章
     *
     * @param userId    用户ID
     * @param badgeId   勋章ID
     * @param reason    授予原因
     */
    void grantUserBadge(Long userId, Long badgeId, String reason);
    
    /**
     * 检查用户是否拥有指定勋章
     *
     * @param userId  用户ID
     * @param badgeId 勋章ID
     * @return 是否拥有
     */
    boolean hasUserBadge(Long userId, Long badgeId);
    
    /**
     * 获取用户积分排行榜
     *
     * @param limit 排行榜数量
     * @return 用户积分排行榜
     */
    List<UserPointEntity> getPointsRanking(int limit);
    
    /**
     * 获取用户等级排行榜
     *
     * @param limit 排行榜数量
     * @return 用户等级排行榜
     */
    List<UserLevelEntity> getLevelRanking(int limit);
}