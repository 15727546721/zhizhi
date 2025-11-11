package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.message.model.entity.UserBlockEntity;
import cn.xu.infrastructure.persistent.po.UserBlock;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户屏蔽转换器
 * 负责用户屏蔽领域实体与持久化对象之间的转换，遵循DDD防腐层模式
 */
@Component
public class UserBlockConverter {
    
    /**
     * 领域实体转换为持久化对象
     */
    public UserBlock toDataObject(UserBlockEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserBlock.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .blockedUserId(entity.getBlockedUserId())
                .createTime(entity.getCreateTime())
                .build();
    }
    
    /**
     * 持久化对象转换为领域实体
     */
    public UserBlockEntity toDomainEntity(UserBlock po) {
        if (po == null) {
            return null;
        }
        return UserBlockEntity.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .blockedUserId(po.getBlockedUserId())
                .createTime(po.getCreateTime())
                .build();
    }
    
    /**
     * 批量转换持久化对象列表为领域实体列表
     */
    public List<UserBlockEntity> toDomainEntities(List<UserBlock> poList) {
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
    public List<UserBlock> toDataObjects(List<UserBlockEntity> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}

