package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.follow.model.entity.UserFollowEntity;
import cn.xu.domain.follow.model.valueobject.FollowStatus;
import cn.xu.infrastructure.persistent.po.Follow;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注关系转换器
 * 负责关注关系领域实体与持久化对象之间的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Component
public class FollowConverter {

    /**
     * 领域实体转换为持久化对象
     */
    public Follow toDataObject(UserFollowEntity entity) {
        if (entity == null) {
            return null;
        }
        return Follow.builder()
                .id(entity.getId())
                .followerId(entity.getFollowerId())
                .followedId(entity.getFollowedId())
                .status(entity.getStatus() != null ? entity.getStatus().getValue() : null)
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 持久化对象转换为领域实体
     */
    public UserFollowEntity toDomainEntity(Follow po) {
        if (po == null) {
            return null;
        }
        return UserFollowEntity.builder()
                .id(po.getId())
                .followerId(po.getFollowerId())
                .followedId(po.getFollowedId())
                .status(po.getStatus() != null ? FollowStatus.valueOf(po.getStatus()) : null)
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    /**
     * 批量转换持久化对象列表为领域实体列表
     */
    public List<UserFollowEntity> toDomainEntities(List<Follow> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换领域实体列表为持久化对象列表
     */
    public List<Follow> toDataObjects(List<UserFollowEntity> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}