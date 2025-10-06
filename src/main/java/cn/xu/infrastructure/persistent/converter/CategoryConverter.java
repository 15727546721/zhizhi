package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.post.model.entity.CategoryEntity;
import cn.xu.infrastructure.persistent.po.PostCategory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类转换器
 * 负责分类领域实体与持久化对象之间的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Component
public class CategoryConverter {

    /**
     * 领域实体转换为持久化对象
     */
    public PostCategory toDataObject(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return PostCategory.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 持久化对象转换为领域实体
     */
    public CategoryEntity toDomainEntity(PostCategory po) {
        if (po == null) {
            return null;
        }
        return CategoryEntity.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    /**
     * 批量转换持久化对象列表为领域实体列表
     */
    public List<CategoryEntity> toDomainEntities(List<PostCategory> poList) {
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
    public List<PostCategory> toDataObjects(List<CategoryEntity> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}