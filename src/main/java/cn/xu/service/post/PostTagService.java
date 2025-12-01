package cn.xu.service.post;

import cn.xu.common.ResponseCode;
import cn.xu.model.entity.Tag;
import cn.xu.repository.IPostTagRepository;
import cn.xu.repository.ITagRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 标签服务实现类（简化版）
 * 
 * 设计改进:
 * 1. 直接操作Tag PO，移除Tag转换
 * 2. 简化实现逻辑，提升性能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostTagService {
    
    private final IPostTagRepository postTagRepository;
    
    private final ITagRepository tagRepository;
    public List<PostTagRelation> batchGetTagIdsByPostIds(List<Long> postIds) {
        try {
            return postTagRepository.batchGetTagIdsByPostIds(postIds);
        } catch (Exception e) {
            log.error("批量获取帖子标签ID列表失败", e);
            return new ArrayList<>();
        }
    }
    public List<Tag> getTagList() {
        try {
            List<Tag> tags = tagRepository.getAllTags();
            return tags != null ? tags : Collections.emptyList();
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return Collections.emptyList();
        }
    }
    public Tag getTagById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            // 直接调用Repository的单查询方法，避免加载全部标签
            return tagRepository.getTagById(id);
        } catch (Exception e) {
            log.error("根据ID获取标签失败, id: {}", id, e);
            return null;
        }
    }
    public Tag createTag(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("创建标签失败: 标签名称不能为空");
            return null;
        }
        try {
            String trimmedName = name.trim();
            tagRepository.addTag(trimmedName);
            // 通过搜索获取刚创建的标签（比获取全部标签更高效）
            List<Tag> tags = tagRepository.searchTags(trimmedName);
            if (tags != null && !tags.isEmpty()) {
                return tags.stream()
                        .filter(tag -> trimmedName.equals(tag.getName()))
                        .findFirst()
                        .orElse(tags.get(0));
            }
            log.warn("创建标签后未能查询到, name: {}", trimmedName);
            return null;
        } catch (Exception e) {
            log.error("创建标签失败, name: {}", name, e);
            return null;
        }
    }
    public Tag updateTag(Long id, String name) {
        // 实现更新标签逻辑
        try {
            if (id == null || name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("标签ID和名称不能为空");
            }
            
            // 调用TagRepository更新标签
            tagRepository.updateTag(id, name.trim());
            
            // 重新获取更新后的标签
            Tag updatedTag = tagRepository.getTagById(id);
            if (updatedTag == null) {
                log.error("更新标签后无法获取标签信息, id: {}", id);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新标签后无法获取标签信息");
            }
            
            log.info("更新标签成功, id: {}, name: {}", id, name);
            return updatedTag;
        } catch (IllegalArgumentException e) {
            log.error("更新标签参数错误, id: {}, name: {}", id, name, e);
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新标签失败, id: {}, name: {}", id, name, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新标签失败: " + e.getMessage());
        }
    }
    public void deleteTag(Long id) {
        // 实现删除标签逻辑
        try {
            if (id == null) {
                throw new IllegalArgumentException("标签ID不能为空");
            }
            
            // 调用TagRepository删除标签（内部会处理关联关系）
            tagRepository.deleteTag(id);
            
            log.info("删除标签成功, id: {}", id);
        } catch (IllegalArgumentException e) {
            log.error("删除标签参数错误, id: {}", id, e);
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除标签失败, id: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除标签失败: " + e.getMessage());
        }
    }
    public List<Tag> getTagsByPostId(Long postId) {
        if (postId == null) {
            return Collections.emptyList();
        }
        try {
            // 通过PostTagRepository获取标签ID列表
            List<Long> tagIds = postTagRepository.getTagIdsByPostId(postId);
            if (tagIds == null || tagIds.isEmpty()) {
                return Collections.emptyList();
            }
            // 批量获取标签详情，避免加载全部标签
            List<Tag> result = new ArrayList<>(tagIds.size());
            for (Long tagId : tagIds) {
                Tag tag = tagRepository.getTagById(tagId);
                if (tag != null) {
                    result.add(tag);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("根据帖子ID获取标签列表失败, postId: {}", postId, e);
            return Collections.emptyList();
        }
    }
    public void savePostTags(Long postId, List<Long> tagIds) {
        // 实现保存帖子标签关联关系逻辑
        postTagRepository.savePostTag(postId, tagIds);
    }
    public List<Tag> searchTags(String keyword) {
        // 实现搜索标签逻辑
        try {
            List<Tag> result = tagRepository.searchTags(keyword);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.error("搜索标签失败, keyword: {}", keyword, e);
            return Collections.emptyList();
        }
    }
    public List<Tag> getHotTags(Integer limit) {
        // 实现获取热门标签逻辑
        try {
            int actualLimit = limit != null ? limit : 10;
            List<Tag> result = tagRepository.getHotTags(actualLimit);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.error("获取热门标签失败, limit: {}", limit, e);
            return Collections.emptyList();
        }
    }
    public List<Tag> getHotTagsByTimeRange(String timeRange, Integer limit) {
        // 实现获取热门标签逻辑（支持时间维度）
        try {
            String actualTimeRange = timeRange != null ? timeRange : "all";
            int actualLimit = limit != null ? limit : 10;
            List<Tag> result = tagRepository.getHotTagsByTimeRange(actualTimeRange, actualLimit);
            // 确保不返回null，返回空列表
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取热门标签失败, timeRange: {}, limit: {}", timeRange, limit, e);
            return new ArrayList<>();
        }
    }

    // ==================== 内部类 ====================
    
    /**
     * 帖子与标签的关联关系
     */
    public static class PostTagRelation {
        private Long postId;
        private List<Long> tagIds;
        
        public PostTagRelation() {}
        
        public PostTagRelation(Long postId, List<Long> tagIds) {
            this.postId = postId;
            this.tagIds = tagIds;
        }
        
        public Long getPostId() { return postId; }
        public void setPostId(Long postId) { this.postId = postId; }
        public List<Long> getTagIds() { return tagIds; }
        public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
    }
    
    /**
     * 标签统计信息
     */
    public static class TagStatistics {
        private Long tagId;
        private String tagName;
        private Integer postCount;
        
        public TagStatistics() {}
        
        public TagStatistics(Long tagId, String tagName, Integer postCount) {
            this.tagId = tagId;
            this.tagName = tagName;
            this.postCount = postCount;
        }
        
        public Long getTagId() { return tagId; }
        public void setTagId(Long tagId) { this.tagId = tagId; }
        public String getTagName() { return tagName; }
        public void setTagName(String tagName) { this.tagName = tagName; }
        public Integer getPostCount() { return postCount; }
        public void setPostCount(Integer postCount) { this.postCount = postCount; }
    }
}