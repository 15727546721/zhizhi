package cn.xu.repository.impl;

import cn.xu.cache.TagCacheRepository;
import cn.xu.common.ResponseCode;
import cn.xu.model.dto.post.PostAndTagAgg;
import cn.xu.model.entity.Tag;
import cn.xu.repository.IPostTagRepository;
import cn.xu.repository.ITagRepository;
import cn.xu.repository.mapper.PostTagMapper;
import cn.xu.repository.mapper.TagMapper;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签仓储实现
 * <p>负责标签数据的持久化操作</p>
 
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TagRepository implements ITagRepository {

    private final TagMapper tagMapper;
    private final IPostTagRepository postTagRepository;
    private final PostTagMapper postTagMapper;
    private final TagCacheRepository tagCacheRepository;

    @Override
    public Long addTag(String name) {
        return tagMapper.addTag(name);
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
    public List<Tag> searchTags(String keyword) {
        return tagMapper.searchTags(keyword);
    }

    @Override
    public List<Tag> getHotTags(int limit) {
        // 默认使用全部时间范围
        return getHotTagsByTimeRange("all", limit);
    }

    @Override
    public List<Tag> getHotTagsByTimeRange(String timeRange, int limit) {
        // 规范化时间范围参数
        String normalizedTimeRange = normalizeTimeRange(timeRange);

        // 先尝试从缓存获取
        List<Tag> cachedTags = tagCacheRepository.getHotTags(normalizedTimeRange, limit);
        if (cachedTags != null && !cachedTags.isEmpty()) {
            log.debug("从缓存获取热门标签 - timeRange={}, limit={}, count={}", normalizedTimeRange, limit, cachedTags.size());
            return cachedTags;
        }

        // 缓存未命中，从数据库查询
        try {
            List<Tag> tags = tagMapper.getHotTagsByTimeRange(normalizedTimeRange, limit);

            // 如果查询结果为空，可能是标签没有被已发布的帖子使用
            // 降级策略：返回所有标签（按创建时间倒序，取前limit个）
            if (tags.isEmpty()) {
                log.warn("热门标签查询结果为空，降级返回所有标签 - timeRange={}, limit={}", normalizedTimeRange, limit);
                tags = tagMapper.getAllTags().stream()
                        .limit(limit)
                        .collect(Collectors.toList());
                log.info("降级查询返回标签数量: {}", tags.size());
            }

            // 写入缓存（即使降级结果也缓存，避免频繁查询）
            tagCacheRepository.cacheHotTags(normalizedTimeRange, limit, tags);

            log.debug("从数据库查询热门标签并缓存 - timeRange={}, limit={}, count={}", normalizedTimeRange, limit, tags.size());
            return tags;
        } catch (Exception e) {
            log.error("获取热门标签失败 - timeRange={}, limit={}", normalizedTimeRange, limit, e);
            // 异常时也尝试降级返回所有标签
            try {
                List<Tag> fallbackTags = tagMapper.getAllTags().stream()
                        .limit(limit)
                        .collect(Collectors.toList());
                log.warn("异常降级返回所有标签 - count={}", fallbackTags.size());
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
    public List<Tag> getAllTags() {
        return tagMapper.getAllTags();
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
    public Tag getTagById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return tagMapper.getTagById(id);
        } catch (Exception e) {
            log.error("根据ID获取标签失败 - id: {}", id, e);
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
            Tag existingTag = getTagById(id);
            if (existingTag == null) {
                throw new IllegalArgumentException("标签不存在，ID: " + id);
            }

            // 更新标签
            tagMapper.updateTag(id, name.trim());
            log.info("更新标签成功 - id: {}, name: {}", id, name);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新标签失败 - id: {}, name: {}", id, name, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新标签失败: " + e.getMessage());
        }
    }

    @Override
    public void updateTag(Tag tag) {
        if (tag == null || tag.getId() == null) {
            throw new IllegalArgumentException("标签对象或ID不能为空");
        }
        try {
            tagMapper.updateTagFull(tag);
            log.info("更新标签成功 - id: {}", tag.getId());
        } catch (Exception e) {
            log.error("更新标签失败 - id: {}", tag.getId(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新标签失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteTag(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        try {
            // 检查标签是否存在
            Tag existingTag = getTagById(id);
            if (existingTag == null) {
                log.warn("标签不存在，ID: {}", id);
                throw new IllegalArgumentException("标签不存在，ID: " + id);
            }

            // 删除标签
            tagMapper.deleteTag(id);
            log.info("删除标签成功 - id: {}", id);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除标签失败 - id: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除标签失败: " + e.getMessage());
        }
    }

    @Override
    public List<Tag> findByIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        return tagMapper.findByIds(tagIds);
    }
}
