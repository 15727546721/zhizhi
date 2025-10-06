package cn.xu.domain.user.service.impl;

import cn.xu.domain.user.model.entity.UserBadgeEntity;
import cn.xu.domain.user.model.entity.UserLevelEntity;
import cn.xu.domain.user.model.entity.UserPointEntity;
import cn.xu.domain.user.repository.IUserIncentiveRepository;
import cn.xu.domain.user.service.IUserIncentiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户激励领域服务实现
 * 处理用户积分、等级、勋章等激励相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserIncentiveService implements IUserIncentiveService {
    
    @Resource
    private IUserIncentiveRepository userIncentiveRepository;
    
    @Override
    public UserPointEntity getUserPoints(Long userId) {
        try {
            UserPointEntity userPointEntity = userIncentiveRepository.findUserPointsByUserId(userId);
            if (userPointEntity == null) {
                // 如果用户还没有积分记录，创建一个新的
                userPointEntity = UserPointEntity.createNewUserPoint(userId);
                userIncentiveRepository.saveUserPoints(userPointEntity);
            }
            return userPointEntity;
        } catch (Exception e) {
            log.error("获取用户积分信息失败 - userId: {}", userId, e);
            throw new RuntimeException("获取用户积分信息失败", e);
        }
    }
    
    @Override
    public void addUserPoints(Long userId, Long points, String reason) {
        log.info("增加用户积分 - userId: {}, points: {}, reason: {}", userId, points, reason);
        
        try {
            UserPointEntity userPointEntity = userIncentiveRepository.findUserPointsByUserId(userId);
            if (userPointEntity == null) {
                userPointEntity = UserPointEntity.createNewUserPoint(userId);
            }
            
            userPointEntity.addPoints(points);
            userIncentiveRepository.saveUserPoints(userPointEntity);
            
            // 记录积分变动日志
            log.info("用户积分增加成功 - userId: {}, points: {}, reason: {}", userId, points, reason);
        } catch (Exception e) {
            log.error("增加用户积分失败 - userId: {}, points: {}, reason: {}", userId, points, reason, e);
            throw new RuntimeException("增加用户积分失败", e);
        }
    }
    
    @Override
    public void consumeUserPoints(Long userId, Long points, String reason) {
        log.info("消费用户积分 - userId: {}, points: {}, reason: {}", userId, points, reason);
        
        try {
            UserPointEntity userPointEntity = userIncentiveRepository.findUserPointsByUserId(userId);
            if (userPointEntity == null) {
                throw new IllegalStateException("用户积分记录不存在");
            }
            
            userPointEntity.consumePoints(points);
            userIncentiveRepository.saveUserPoints(userPointEntity);
            
            // 记录积分变动日志
            log.info("用户积分消费成功 - userId: {}, points: {}, reason: {}", userId, points, reason);
        } catch (Exception e) {
            log.error("消费用户积分失败 - userId: {}, points: {}, reason: {}", userId, points, reason, e);
            throw new RuntimeException("消费用户积分失败", e);
        }
    }
    
    @Override
    public UserLevelEntity getUserLevel(Long userId) {
        try {
            UserLevelEntity userLevelEntity = userIncentiveRepository.findUserLevelByUserId(userId);
            if (userLevelEntity == null) {
                // 如果用户还没有等级记录，创建一个新的
                userLevelEntity = UserLevelEntity.createNewUserLevel(userId);
                userIncentiveRepository.saveUserLevel(userLevelEntity);
            }
            return userLevelEntity;
        } catch (Exception e) {
            log.error("获取用户等级信息失败 - userId: {}", userId, e);
            throw new RuntimeException("获取用户等级信息失败", e);
        }
    }
    
    @Override
    public void addUserExp(Long userId, Long exp, String reason) {
        log.info("增加用户经验值 - userId: {}, exp: {}, reason: {}", userId, exp, reason);
        
        try {
            UserLevelEntity userLevelEntity = userIncentiveRepository.findUserLevelByUserId(userId);
            if (userLevelEntity == null) {
                userLevelEntity = UserLevelEntity.createNewUserLevel(userId);
            }
            
            userLevelEntity.addExp(exp);
            userIncentiveRepository.saveUserLevel(userLevelEntity);
            
            // 记录经验值变动日志
            log.info("用户经验值增加成功 - userId: {}, exp: {}, reason: {}", userId, exp, reason);
        } catch (Exception e) {
            log.error("增加用户经验值失败 - userId: {}, exp: {}, reason: {}", userId, exp, reason, e);
            throw new RuntimeException("增加用户经验值失败", e);
        }
    }
    
    @Override
    public List<UserBadgeEntity> getUserBadges(Long userId) {
        try {
            return userIncentiveRepository.findUserBadgesByUserId(userId);
        } catch (Exception e) {
            log.error("获取用户勋章列表失败 - userId: {}", userId, e);
            throw new RuntimeException("获取用户勋章列表失败", e);
        }
    }
    
    @Override
    public void grantUserBadge(Long userId, Long badgeId, String reason) {
        log.info("授予用户勋章 - userId: {}, badgeId: {}, reason: {}", userId, badgeId, reason);
        
        try {
            // 检查用户是否已经拥有该勋章
            if (hasUserBadge(userId, badgeId)) {
                log.info("用户已拥有该勋章 - userId: {}, badgeId: {}", userId, badgeId);
                return;
            }
            
            // 获取勋章信息（这里简化处理，实际应该从勋章配置表中获取）
            String badgeName = "成就勋章";
            String badgeDescription = "恭喜获得成就";
            String badgeIcon = "/icons/badge.png";
            
            UserBadgeEntity userBadgeEntity = UserBadgeEntity.createNewUserBadge(
                    userId, badgeId, badgeName, badgeDescription, badgeIcon);
            userBadgeEntity.obtainBadge();
            userIncentiveRepository.saveUserBadge(userBadgeEntity);
            
            // 记录勋章授予日志
            log.info("用户勋章授予成功 - userId: {}, badgeId: {}, reason: {}", userId, badgeId, reason);
        } catch (Exception e) {
            log.error("授予用户勋章失败 - userId: {}, badgeId: {}, reason: {}", userId, badgeId, reason, e);
            throw new RuntimeException("授予用户勋章失败", e);
        }
    }
    
    @Override
    public boolean hasUserBadge(Long userId, Long badgeId) {
        try {
            return userIncentiveRepository.existsUserBadge(userId, badgeId);
        } catch (Exception e) {
            log.error("检查用户勋章失败 - userId: {}, badgeId: {}", userId, badgeId, e);
            return false;
        }
    }
    
    @Override
    public List<UserPointEntity> getPointsRanking(int limit) {
        try {
            return userIncentiveRepository.findPointsRanking(limit);
        } catch (Exception e) {
            log.error("获取用户积分排行榜失败 - limit: {}", limit, e);
            throw new RuntimeException("获取用户积分排行榜失败", e);
        }
    }
    
    @Override
    public List<UserLevelEntity> getLevelRanking(int limit) {
        try {
            return userIncentiveRepository.findLevelRanking(limit);
        } catch (Exception e) {
            log.error("获取用户等级排行榜失败 - limit: {}", limit, e);
            throw new RuntimeException("获取用户等级排行榜失败", e);
        }
    }
}