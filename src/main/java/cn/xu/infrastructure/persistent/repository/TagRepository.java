package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.post.model.aggregate.PostAndTagAgg;
import cn.xu.domain.post.model.entity.TagEntity;
import cn.xu.domain.post.repository.IPostTagRepository;
import cn.xu.domain.post.repository.ITagRepository;
import cn.xu.infrastructure.cache.TagCacheRepository;
import cn.xu.infrastructure.persistent.converter.TagConverter;
import cn.xu.infrastructure.persistent.dao.PostTagMapper;
import cn.xu.infrastructure.persistent.dao.TagMapper;
import cn.xu.infrastructure.persistent.po.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TagRepository implements ITagRepository {

    private final TagMapper tagMapper;
    private final IPostTagRepository postTagRepository;
    private final PostTagMapper postTagMapper;
    private final TagCacheRepository tagCacheRepository;
    private final TagConverter tagConverter = TagConverter.INSTANCE;

    @Override
    public void addTag(String name) {
        tagMapper.addTag(name);
    }

    @Override
    public List<String> getTagNamesByPostId(Long postId) {
        return tagMapper.getTagNamesByPostId(postId);
    }

    @Override
    public List<PostAndTagAgg> selectByPostIds(List<Long> postIds) {
        return tagMapper.selectByPostIds(postIds);
    }
    
    @Override
    public List<TagEntity> searchTags(String keyword) {
        List<Tag> tags = tagMapper.searchTags(keyword);
        return tags.stream()
                .map(tagConverter::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TagEntity> getHotTags(int limit) {
        // 默认使用全部时间范围
        return getHotTagsByTimeRange("all", limit);
    }
    
    @Override
    public List<TagEntity> getHotTagsByTimeRange(String timeRange, int limit) {
        // 规范化时间范围参数
        String normalizedTimeRange = normalizeTimeRange(timeRange);
        
        // 先尝试从缓存获取
        List<TagEntity> cachedTags = tagCacheRepository.getHotTags(normalizedTimeRange, limit);
        if (cachedTags != null && !cachedTags.isEmpty()) {
            log.debug("从缓存获取热门标签: timeRange={}, limit={}, count={}", normalizedTimeRange, limit, cachedTags.size());
            return cachedTags;
        }
        
        // 缓存未命中，从数据库查询
        try {
            List<Tag> tags = tagMapper.getHotTagsByTimeRange(normalizedTimeRange, limit);
            List<TagEntity> tagEntities = tags.stream()
                    .map(tagConverter::toDomainEntity)
                    .collect(Collectors.toList());
            
            // 如果查询结果为空，可能是标签没有被已发布的帖子使用
            // 降级策略：返回所有标签（按创建时间倒序，取前limit个）
            if (tagEntities.isEmpty()) {
                log.warn("热门标签查询结果为空，降级返回所有标签: timeRange={}, limit={}", normalizedTimeRange, limit);
                List<Tag> allTags = tagMapper.getAllTags();
                tagEntities = allTags.stream()
                        .limit(limit)
                        .map(tagConverter::toDomainEntity)
                        .collect(Collectors.toList());
                log.info("降级查询返回标签数量: {}", tagEntities.size());
            }
            
            // 写入缓存（即使降级结果也缓存，避免频繁查询）
            tagCacheRepository.cacheHotTags(normalizedTimeRange, limit, tagEntities);
            
            log.debug("从数据库查询热门标签并缓存: timeRange={}, limit={}, count={}", normalizedTimeRange, limit, tagEntities.size());
            return tagEntities;
        } catch (Exception e) {
            log.error("获取热门标签失败: timeRange={}, limit={}", normalizedTimeRange, limit, e);
            // 异常时也尝试降级返回所有标签
            try {
                List<Tag> allTags = tagMapper.getAllTags();
                List<TagEntity> fallbackTags = allTags.stream()
                        .limit(limit)
                        .map(tagConverter::toDomainEntity)
                        .collect(Collectors.toList());
                log.warn("异常降级返回所有标签: count={}", fallbackTags.size());
                return fallbackTags;
            } catch (Exception ex) {
                log.error("降级查询也失败", ex);
                return new ArrayList<>();
            }
        }
    }
    
    /**
     * 规范化时间范围参数
     * @param timeRange 原始时间范围
     * @return 规范化后的时间范围
     */
    private String normalizeTimeRange(String timeRange) {
        if (timeRange == null || timeRange.trim().isEmpty()) {
            return "all";
        }
        
        String normalized = timeRange.toLowerCase().trim();
        // 支持多种输入格式
        switch (normalized) {
            case "today":
            case "day":
            case "今日":
                return "today";
            case "week":
            case "weekly":
            case "本周":
                return "week";
            case "month":
            case "monthly":
            case "本月":
                return "month";
            case "all":
            case "全部":
            default:
                return "all";
        }
    }
    
    @Override
    public List<TagEntity> getAllTags() {
        List<Tag> tags = tagMapper.getAllTags();
        return tags.stream()
                .map(tagConverter::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Long> findTagIdsByPostId(Long postId) {
        if (postId == null) {
            return new ArrayList<>();
        }
        // 通过PostTagRepository获取标签ID列表
        return postTagRepository.getTagIdsByPostId(postId);
    }
    
    @Override
    public TagEntity getTagById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            Tag tag = tagMapper.getTagById(id);
            return tag != null ? tagConverter.toDomainEntity(tag) : null;
        } catch (Exception e) {
            log.error("根据ID获取标签失败, id: {}", id, e);
            return null;
        }
    }
    
    @Override
    public void updateTag(Long id, String name) {
        if (id == null || name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("标签ID和名称不能为空");
        }
        try {
            // 检查标签是否存在
            TagEntity existingTag = getTagById(id);
            if (existingTag == null) {
                throw new IllegalArgumentException("标签不存在，ID: " + id);
            }
            
            // 更新标签
            tagMapper.updateTag(id, name.trim());
            log.info("更新标签成功, id: {}, name: {}", id, name);
        } catch (Exception e) {
            log.error("更新标签失败, id: {}, name: {}", id, name, e);
            throw new RuntimeException("更新标签失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteTag(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        try {
            // 检查标签是否存在
            TagEntity existingTag = getTagById(id);
            if (existingTag == null) {
                log.warn("删除标签失败，标签不存在, id: {}", id);
                throw new IllegalArgumentException("标签不存在，ID: " + id);
            }
            
            // 检查标签是否被使用
            int usageCount = postTagMapper.countByTagId(id);
            if (usageCount > 0) {
                log.info("标签被使用，先删除关联关系, id: {}, usageCount: {}", id, usageCount);
                // 先删除标签与帖子的关联关系
                postTagMapper.deleteByTagId(id);
                log.info("删除标签关联关系成功, id: {}", id);
            }
            
            // 删除标签
            tagMapper.deleteTag(id);
            log.info("删除标签成功, id: {}", id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除标签失败, id: {}", id, e);
            throw new RuntimeException("删除标签失败: " + e.getMessage(), e);
        }
    }
}