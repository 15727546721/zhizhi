package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.post.model.entity.TagEntity;
import cn.xu.infrastructure.persistent.po.Tag;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签转换器
 * 负责标签领域实体与持久化对象之间的转换
 */
public class TagConverter {
    
    public static final TagConverter INSTANCE = new TagConverter();
    
    private TagConverter() {}
    
    /**
     * 将TagEntity转换为Tag PO对象
     */
    public Tag toDataObject(TagEntity tagEntity) {
        if (tagEntity == null) {
            return null;
        }
        
        return Tag.builder()
                .id(tagEntity.getId())
                .name(tagEntity.getName())
                .createTime(tagEntity.getCreateTime())
                .updateTime(tagEntity.getUpdateTime())
                // PO中存在但Entity中不存在的字段设置为默认值
                .description(null)
                .isRecommended(0)
                .usageCount(0)
                .build();
    }
    
    /**
     * 将Tag PO对象转换为TagEntity
     */
    public TagEntity toDomainEntity(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        return TagEntity.builder()
                .id(tag.getId())
                .name(tag.getName())
                .createTime(tag.getCreateTime())
                .updateTime(tag.getUpdateTime())
                .build();
    }
    
    /**
     * 批量转换：将Tag PO对象列表转换为TagEntity列表
     */
    public List<TagEntity> toDomainEntityList(List<Tag> tags) {
        if (tags == null) {
            return null;
        }
        
        return tags.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }
}