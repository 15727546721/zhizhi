package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.like.model.LikeEntity;
import cn.xu.domain.like.model.LikeStatus;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.model.aggregate.LikeAggregate;
import cn.xu.infrastructure.persistent.po.Like;
import org.springframework.stereotype.Component;

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
     * 将聚合根转换为持久化对象
     *
     * @param aggregate 点赞聚合根
     * @return 点赞持久化对象
     */
    public Like toDataObjectFromAggregate(LikeAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        
        return Like.builder()
                .id(aggregate.getId())
                .userId(aggregate.getUserId())
                .targetId(aggregate.getTargetId())
                .type(aggregate.getType() != null ? aggregate.getType().getCode() : null)
                .status(aggregate.isLiked() ? LikeStatus.LIKED.getCode() : LikeStatus.UNLIKED.getCode())
                .createTime(aggregate.getCreateTime())
                .build();
    }

    /**
     * 批量转换聚合根为持久化对象
     *
     * @param aggregateList 聚合根列表
     * @return 持久化对象列表
     */
    public List<Like> toDataObjectsFromAggregates(List<LikeAggregate> aggregateList) {
        if (aggregateList == null || aggregateList.isEmpty()) {
            return Collections.emptyList();
        }
        
        return aggregateList.stream()
                .map(this::toDataObjectFromAggregate)
                .collect(Collectors.toList());
    }
    
    /**
     * 将实体转换为持久化对象
     *
     * @param entity 点赞实体
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
                .status(entity.isLiked() ? LikeStatus.LIKED.getCode() : LikeStatus.UNLIKED.getCode())
                .createTime(entity.getCreateTime())
                .build();
    }
    
    /**
     * 将持久化对象转换为实体
     *
     * @param po 点赞持久化对象
     * @return 点赞实体
     */
    public LikeEntity toDomainEntity(Like po) {
        if (po == null) {
            return null;
        }
        
        LikeEntity entity = new LikeEntity();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setTargetId(po.getTargetId());
        entity.setType(po.getType() != null ? LikeType.valueOf(po.getType()) : null);
        entity.setStatus(po.getStatus() != null && po.getStatus() == 1 ? LikeStatus.LIKED : LikeStatus.UNLIKED);
        entity.setCreateTime(po.getCreateTime());
        
        return entity;
    }
}