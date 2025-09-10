package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.like.model.LikeEntity;
import cn.xu.domain.like.model.LikeStatus;
import cn.xu.domain.like.model.LikeType;
import cn.xu.infrastructure.persistent.po.Like;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 点赞领域实体与持久化对象转换器
 * 符合DDD架构的防腐层模式
 * 
 * @author xu
 */
@Component
public class LikeConverter {

    /**
     * 将领域实体转换为持久化对象
     *
     * @param entity 点赞领域实体
     * @return 点赞持久化对象
     */
    public Like toDataObject(LikeEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Like.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .targetId(entity.getTargetId())
                .type(entity.getType() != null ? entity.getType().getCode() : null)
                .status(entity.getStatus() != null ? entity.getStatus().getCode() : null)
                .createTime(entity.getCreateTime())
                .build();
    }

    /**
     * 将持久化对象转换为领域实体
     *
     * @param po 点赞持久化对象
     * @return 点赞领域实体
     */
    public LikeEntity toDomainEntity(Like po) {
        if (po == null) {
            return null;
        }
        
        return LikeEntity.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .targetId(po.getTargetId())
                .type(po.getType() != null ? LikeType.valueOf(po.getType()) : null)
                .status(po.getStatus() != null ? LikeStatus.valueOf(po.getStatus()) : null)
                .createTime(po.getCreateTime())
                .build();
    }

    /**
     * 批量转换持久化对象为领域实体
     *
     * @param poList 持久化对象列表
     * @return 领域实体列表
     */
    public List<LikeEntity> toDomainEntities(List<Like> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        
        return poList.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换领域实体为持久化对象
     *
     * @param entityList 领域实体列表
     * @return 持久化对象列表
     */
    public List<Like> toDataObjects(List<LikeEntity> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return Collections.emptyList();
        }
        
        return entityList.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}