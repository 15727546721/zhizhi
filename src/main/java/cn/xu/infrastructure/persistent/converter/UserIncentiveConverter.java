package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.user.model.entity.*;
import cn.xu.infrastructure.persistent.po.*;
import org.springframework.stereotype.Component;

/**
 * 用户激励领域实体与持久化对象转换器
 * 符合DDD架构的防腐层模式
 */
@Component
public class UserIncentiveConverter {
    
    /**
     * 将用户积分持久化对象转换为领域实体
     */
    public UserPointEntity toUserPointEntity(UserPoint po) {
        if (po == null) {
            return null;
        }
        
        return UserPointEntity.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .totalPoints(po.getTotalPoints())
                .availablePoints(po.getAvailablePoints())
                .consumedPoints(po.getConsumedPoints())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
    
    /**
     * 将用户积分领域实体转换为持久化对象
     */
    public UserPoint toUserPointPO(UserPointEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return UserPoint.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .totalPoints(entity.getTotalPoints())
                .availablePoints(entity.getAvailablePoints())
                .consumedPoints(entity.getConsumedPoints())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
    
    /**
     * 将用户等级持久化对象转换为领域实体
     */
    public UserLevelEntity toUserLevelEntity(UserLevel po) {
        if (po == null) {
            return null;
        }
        
        return UserLevelEntity.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .level(po.getLevel())
                .levelName(po.getLevelName())
                .currentExp(po.getCurrentExp())
                .nextLevelExp(po.getNextLevelExp())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
    
    /**
     * 将用户等级领域实体转换为持久化对象
     */
    public UserLevel toUserLevelPO(UserLevelEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return UserLevel.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .level(entity.getLevel())
                .levelName(entity.getLevelName())
                .currentExp(entity.getCurrentExp())
                .nextLevelExp(entity.getNextLevelExp())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
    
    /**
     * 将用户勋章持久化对象转换为领域实体
     */
    public UserBadgeEntity toUserBadgeEntity(UserBadge po) {
        if (po == null) {
            return null;
        }
        
        return UserBadgeEntity.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .badgeId(po.getBadgeId())
                .badgeName(po.getBadgeName())
                .badgeDescription(po.getBadgeDescription())
                .badgeIcon(po.getBadgeIcon())
                .status(po.getStatus())
                .obtainTime(po.getObtainTime())
                .expireTime(po.getExpireTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
    
    /**
     * 将用户勋章领域实体转换为持久化对象
     */
    public UserBadge toUserBadgePO(UserBadgeEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return UserBadge.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .badgeId(entity.getBadgeId())
                .badgeName(entity.getBadgeName())
                .badgeDescription(entity.getBadgeDescription())
                .badgeIcon(entity.getBadgeIcon())
                .status(entity.getStatus())
                .obtainTime(entity.getObtainTime())
                .expireTime(entity.getExpireTime())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
    
    /**
     * 将积分流水持久化对象转换为领域实体
     */
    public PointTransactionEntity toPointTransactionEntity(PointTransaction po) {
        if (po == null) {
            return null;
        }
        
        return PointTransactionEntity.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .changeAmount(po.getChangeAmount())
                .changeType(po.getChangeType())
                .orderId(po.getOrderId())
                .description(po.getDescription())
                .balanceAfter(po.getBalanceAfter())
                .status(po.getStatus())
                .relatedId(po.getRelatedId())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
    
    /**
     * 将积分流水领域实体转换为持久化对象
     */
    public PointTransaction toPointTransactionPO(PointTransactionEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return PointTransaction.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .changeAmount(entity.getChangeAmount())
                .changeType(entity.getChangeType())
                .orderId(entity.getOrderId())
                .description(entity.getDescription())
                .balanceAfter(entity.getBalanceAfter())
                .status(entity.getStatus())
                .relatedId(entity.getRelatedId())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
    
    /**
     * 将会员持久化对象转换为领域实体
     */
    public MemberEntity toMemberEntity(Member po) {
        if (po == null) {
            return null;
        }
        
        return MemberEntity.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .currentPoints(po.getCurrentPoints())
                .totalEarnedPoints(po.getTotalEarnedPoints())
                .level(po.getLevel())
                .levelName(po.getLevelName())
                .currentExp(po.getCurrentExp())
                .nextLevelExp(po.getNextLevelExp())
                .status(po.getStatus())
                .levelUpdatedAt(po.getLevelUpdatedAt())
                .lastEarnedAt(po.getLastEarnedAt())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
    
    /**
     * 将会员领域实体转换为持久化对象
     */
    public Member toMemberPO(MemberEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Member.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .currentPoints(entity.getCurrentPoints())
                .totalEarnedPoints(entity.getTotalEarnedPoints())
                .level(entity.getLevel())
                .levelName(entity.getLevelName())
                .currentExp(entity.getCurrentExp())
                .nextLevelExp(entity.getNextLevelExp())
                .status(entity.getStatus())
                .levelUpdatedAt(entity.getLevelUpdatedAt())
                .lastEarnedAt(entity.getLastEarnedAt())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
    
    /**
     * 将积分变动类型配置持久化对象转换为领域实体
     */
    public PointChangeTypeConfigEntity toPointChangeTypeConfigEntity(PointChangeTypeConfig po) {
        if (po == null) {
            return null;
        }
        
        return PointChangeTypeConfigEntity.builder()
                .id(po.getId())
                .changeType(po.getChangeType())
                .changeName(po.getChangeName())
                .pointValue(po.getPointValue())
                .dailyLimit(po.getDailyLimit())
                .description(po.getDescription())
                .isActive(po.getIsActive())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
    
    /**
     * 将积分变动类型配置领域实体转换为持久化对象
     */
    public PointChangeTypeConfig toPointChangeTypeConfigPO(PointChangeTypeConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return PointChangeTypeConfig.builder()
                .id(entity.getId())
                .changeType(entity.getChangeType())
                .changeName(entity.getChangeName())
                .pointValue(entity.getPointValue())
                .dailyLimit(entity.getDailyLimit())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}