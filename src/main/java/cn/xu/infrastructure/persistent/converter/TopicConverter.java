package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.essay.model.entity.TopicEntity;
import cn.xu.infrastructure.persistent.po.Topic;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 话题领域实体与持久化对象转换器
 * 符合DDD架构的防腐层模式
 * 
 * @author xu
 */
@Component
public class TopicConverter {

    /**
     * 将领域实体转换为持久化对象
     *
     * @param entity 话题领域实体
     * @return 话题持久化对象
     */
    public Topic toDataObject(TopicEntity entity) {
        if (entity == null) {
            return null;
        }

        return Topic.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 将持久化对象转换为领域实体
     *
     * @param po 话题持久化对象
     * @return 话题领域实体
     */
    public TopicEntity toDomainEntity(Topic po) {
        if (po == null) {
            return null;
        }

        return TopicEntity.builder()
                .id(po.getId())
                .name(po.getName())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    /**
     * 批量转换持久化对象为领域实体
     *
     * @param pos 持久化对象列表
     * @return 领域实体列表
     */
    public List<TopicEntity> toDomainEntities(List<Topic> pos) {
        if (pos == null || pos.isEmpty()) {
            return Collections.emptyList();
        }

        return pos.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换领域实体为持久化对象
     *
     * @param entities 领域实体列表
     * @return 持久化对象列表
     */
    public List<Topic> toDataObjects(List<TopicEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}