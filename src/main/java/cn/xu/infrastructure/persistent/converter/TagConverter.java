package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.infrastructure.persistent.po.ArticleTag;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签领域实体与持久化对象转换器
 * 符合DDD架构的防腐层模式
 * 
 * @author xu
 */
@Component
public class TagConverter {

    /**
     * 将领域实体转换为持久化对象
     *
     * @param entity 标签领域实体
     * @return 标签持久化对象
     */
    public ArticleTag toDataObject(TagEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return ArticleTag.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    /**
     * 将持久化对象转换为领域实体
     *
     * @param po 标签持久化对象
     * @return 标签领域实体
     */
    public TagEntity toDomainEntity(ArticleTag po) {
        if (po == null) {
            return null;
        }
        
        return TagEntity.builder()
                .id(po.getId())
                .name(po.getName())
                .build();
    }

    /**
     * 批量转换持久化对象为领域实体
     *
     * @param poList 持久化对象列表
     * @return 领域实体列表
     */
    public List<TagEntity> toDomainEntities(List<ArticleTag> poList) {
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
     * @param entities 领域实体列表
     * @return 持久化对象列表
     */
    public List<ArticleTag> toDataObjects(List<TagEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        
        return entities.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}