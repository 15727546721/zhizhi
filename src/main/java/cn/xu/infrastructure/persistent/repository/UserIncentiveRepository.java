package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.user.model.entity.UserBadgeEntity;
import cn.xu.domain.user.model.entity.UserLevelEntity;
import cn.xu.domain.user.model.entity.UserPointEntity;
import cn.xu.domain.user.repository.IUserIncentiveRepository;
import cn.xu.infrastructure.persistent.converter.UserIncentiveConverter;
import cn.xu.infrastructure.persistent.dao.UserBadgeMapper;
import cn.xu.infrastructure.persistent.dao.UserLevelMapper;
import cn.xu.infrastructure.persistent.dao.UserPointMapper;
import cn.xu.infrastructure.persistent.po.UserBadge;
import cn.xu.infrastructure.persistent.po.UserLevel;
import cn.xu.infrastructure.persistent.po.UserPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户激励仓储实现
 * 遵循DDD原则，通过转换器处理领域实体与持久化对象的转换
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserIncentiveRepository implements IUserIncentiveRepository {
    
    @Resource
    private UserPointMapper userPointMapper;
    
    @Resource
    private UserLevelMapper userLevelMapper;
    
    @Resource
    private UserBadgeMapper userBadgeMapper;
    
    @Resource
    private UserIncentiveConverter userIncentiveConverter;
    
    @Override
    public UserPointEntity findUserPointsByUserId(Long userId) {
        UserPoint userPoint = userPointMapper.findByUserId(userId);
        return userIncentiveConverter.toUserPointEntity(userPoint);
    }
    
    @Override
    public void saveUserPoints(UserPointEntity userPointEntity) {
        UserPoint userPoint = userIncentiveConverter.toUserPointPO(userPointEntity);
        if (userPoint.getId() == null) {
            userPointMapper.insert(userPoint);
            userPointEntity.setId(userPoint.getId());
        } else {
            userPointMapper.update(userPoint);
        }
    }
    
    @Override
    public UserLevelEntity findUserLevelByUserId(Long userId) {
        UserLevel userLevel = userLevelMapper.findByUserId(userId);
        return userIncentiveConverter.toUserLevelEntity(userLevel);
    }
    
    @Override
    public void saveUserLevel(UserLevelEntity userLevelEntity) {
        UserLevel userLevel = userIncentiveConverter.toUserLevelPO(userLevelEntity);
        if (userLevel.getId() == null) {
            userLevelMapper.insert(userLevel);
            userLevelEntity.setId(userLevel.getId());
        } else {
            userLevelMapper.update(userLevel);
        }
    }
    
    @Override
    public List<UserBadgeEntity> findUserBadgesByUserId(Long userId) {
        List<UserBadge> userBadges = userBadgeMapper.findByUserId(userId);
        return userBadges.stream()
                .map(userIncentiveConverter::toUserBadgeEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public void saveUserBadge(UserBadgeEntity userBadgeEntity) {
        UserBadge userBadge = userIncentiveConverter.toUserBadgePO(userBadgeEntity);
        if (userBadge.getId() == null) {
            userBadgeMapper.insert(userBadge);
            userBadgeEntity.setId(userBadge.getId());
        } else {
            userBadgeMapper.update(userBadge);
        }
    }
    
    @Override
    public boolean existsUserBadge(Long userId, Long badgeId) {
        return userBadgeMapper.existsByUserIdAndBadgeId(userId, badgeId);
    }
    
    @Override
    public List<UserPointEntity> findPointsRanking(int limit) {
        List<UserPoint> userPoints = userPointMapper.findRanking(limit);
        return userPoints.stream()
                .map(userIncentiveConverter::toUserPointEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserLevelEntity> findLevelRanking(int limit) {
        List<UserLevel> userLevels = userLevelMapper.findRanking(limit);
        return userLevels.stream()
                .map(userIncentiveConverter::toUserLevelEntity)
                .collect(Collectors.toList());
    }
}