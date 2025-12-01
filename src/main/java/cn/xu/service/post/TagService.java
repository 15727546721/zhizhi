package cn.xu.service.post;

import cn.xu.model.entity.Tag;
import cn.xu.repository.IPostTagRepository;
import cn.xu.repository.ITagRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务（统一架构）
 * 
 * 架构改进：
 * 1. 整合PostTagService + PostTagService
 * 2. 直接操作Tag PO，移除Entity转换
 * 3. 统一标签CRUD、帖子关联、热门标签等功能
 * 4. 使用@Service直接实现，移除接口层
 * 
 * @author xu
 * @since 2025-11-24
 */
@Slf4j
@Service("tagService")
@RequiredArgsConstructor
public class TagService {
    
    private final ITagRepository tagRepository;
    private final IPostTagRepository postTagRepository;
    
    // ==================== 标签CRUD ====================
    
    /**
     * 获取所有标签列表
     */
    public List<Tag> getTagList() {
        try {
            List<Tag> tags = tagRepository.getAllTags();
            return tags != null ? tags : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据ID获取标签
     */
    public Tag getTagById(Long id) {
        if (id == null) {
            throw new BusinessException("标签ID不能为空");
        }
        try {
            Tag tag = tagRepository.getTagById(id);
            if (tag == null) {
                throw new BusinessException("标签不存在");
            }
            return tag;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("根据ID获取标签失败, id: {}", id, e);
            throw new BusinessException("获取标签失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建标签
     */
    @Transactional(rollbackFor = Exception.class)
    public Tag createTag(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("标签名称不能为空");
        }
        
        try {
            String trimmedName = name.trim();
            
            // 检查标签是否已存在
            List<Tag> existingTags = searchTags(trimmedName);
            if (existingTags != null && existingTags.stream().anyMatch(tag -> trimmedName.equals(tag.getName()))) {
                throw new BusinessException("标签已存在：" + trimmedName);
            }
            
            // 创建新标签
            tagRepository.addTag(trimmedName);
            
            // 重新获取创建的标签
            List<Tag> allTags = tagRepository.getAllTags();
            if (allTags != null) {
                Tag newTag = allTags.stream()
                        .filter(tag -> trimmedName.equals(tag.getName()))
                        .findFirst()
                        .orElse(null);
                
                if (newTag != null) {
                    log.info("创建标签成功, name: {}, id: {}", trimmedName, newTag.getId());
                    return newTag;
                }
            }
            
            throw new BusinessException("创建标签后获取失败");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建标签失败, name: {}", name, e);
            throw new BusinessException("创建标签失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新标签
     */
    @Transactional(rollbackFor = Exception.class)
    public Tag updateTag(Long id, String name) {
        if (id == null || name == null || name.trim().isEmpty()) {
            throw new BusinessException("标签ID和名称不能为空");
        }
        
        try {
            String trimmedName = name.trim();
            
            // 检查标签是否存在
            Tag existingTag = getTagById(id);
            if (existingTag == null) {
                throw new BusinessException("标签不存在，ID：" + id);
            }
            
            // 检查新名称是否与其他标签重复
            List<Tag> duplicateTags = searchTags(trimmedName);
            if (duplicateTags != null) {
                boolean hasDuplicate = duplicateTags.stream()
                        .anyMatch(tag -> !tag.getId().equals(id) && trimmedName.equals(tag.getName()));
                if (hasDuplicate) {
                    throw new BusinessException("标签名称已存在：" + trimmedName);
                }
            }
            
            // 更新标签
            tagRepository.updateTag(id, trimmedName);
            
            // 重新获取更新后的标签
            Tag updatedTag = getTagById(id);
            if (updatedTag == null) {
                throw new BusinessException("更新标签后获取失败");
            }
            
            log.info("更新标签成功, id: {}, name: {} -> {}", id, existingTag.getName(), trimmedName);
            return updatedTag;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新标签失败, id: {}, name: {}", id, name, e);
            throw new BusinessException("更新标签失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新标签（完整对象）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(Tag tag) {
        if (tag == null || tag.getId() == null) {
            throw new BusinessException("标签对象或ID不能为空");
        }
        
        try {
            // 检查标签是否存在
            Tag existingTag = getTagById(tag.getId());
            if (existingTag == null) {
                throw new BusinessException("标签不存在，ID：" + tag.getId());
            }
            
            // 如果修改了名称，检查是否重复
            if (tag.getName() != null && !tag.getName().trim().isEmpty()) {
                String trimmedName = tag.getName().trim();
                if (!trimmedName.equals(existingTag.getName())) {
                    List<Tag> duplicateTags = searchTags(trimmedName);
                    if (duplicateTags != null) {
                        boolean hasDuplicate = duplicateTags.stream()
                                .anyMatch(t -> !t.getId().equals(tag.getId()) && trimmedName.equals(t.getName()));
                        if (hasDuplicate) {
                            throw new BusinessException("标签名称已存在：" + trimmedName);
                        }
                    }
                }
                tag.setName(trimmedName);
            } else {
                // 保持原名称
                tag.setName(existingTag.getName());
            }
            
            // 更新标签
            tagRepository.updateTag(tag);
            log.info("更新标签成功, id: {}", tag.getId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新标签失败, id: {}", tag.getId(), e);
            throw new BusinessException("更新标签失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        if (id == null) {
            throw new BusinessException("标签ID不能为空");
        }
        
        try {
            // 检查标签是否存在
            Tag existingTag = getTagById(id);
            if (existingTag == null) {
                throw new BusinessException("标签不存在，ID：" + id);
            }
            
            // 删除标签（Repository内部会处理关联关系）
            tagRepository.deleteTag(id);
            
            log.info("删除标签成功, id: {}, name: {}", id, existingTag.getName());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除标签失败, id: {}", id, e);
            throw new BusinessException("删除标签失败: " + e.getMessage());
        }
    }

    /**
     * 切换标签推荐状态（置顶/取消置顶）
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleRecommended(Long id) {
        if (id == null) {
            throw new BusinessException("标签ID不能为空");
        }
        
        try {
            Tag tag = getTagById(id);
            if (tag == null) {
                throw new BusinessException("标签不存在，ID：" + id);
            }
            
            // 切换推荐状态
            int newStatus = (tag.getIsRecommended() == null || tag.getIsRecommended() == 0) ? 1 : 0;
            tag.setIsRecommended(newStatus);
            tag.setUpdateTime(java.time.LocalDateTime.now());
            tagRepository.updateTag(tag);
            
            log.info("切换标签推荐状态成功, id: {}, isRecommended: {}", id, newStatus);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("切换标签推荐状态失败, id: {}", id, e);
            throw new BusinessException("操作失败: " + e.getMessage());
        }
    }
    
    // ==================== 帖子标签关联 ====================
    
    /**
     * 根据帖子ID获取标签列表
     */
    public List<Tag> getTagsByPostId(Long postId) {
        if (postId == null) {
            return Collections.emptyList();
        }
        
        try {
            // 获取标签ID列表
            List<Long> tagIds = postTagRepository.getTagIdsByPostId(postId);
            if (tagIds == null || tagIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 获取标签详情
            List<Tag> allTags = getTagList();
            return allTags.stream()
                    .filter(tag -> tagIds.contains(tag.getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据帖子ID获取标签列表失败, postId: {}", postId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 保存帖子标签关联关系
     */
    @Transactional(rollbackFor = Exception.class)
    public void savePostTags(Long postId, List<Long> tagIds) {
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        
        try {
            // 验证标签ID有效性
            if (tagIds != null && !tagIds.isEmpty()) {
                List<Tag> allTags = getTagList();
                List<Long> validTagIds = allTags.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toList());
                
                List<Long> invalidIds = tagIds.stream()
                        .filter(id -> !validTagIds.contains(id))
                        .collect(Collectors.toList());
                
                if (!invalidIds.isEmpty()) {
                    throw new BusinessException("无效的标签ID：" + invalidIds);
                }
            }
            
            // 保存关联关系
            postTagRepository.savePostTag(postId, tagIds);
            log.info("保存帖子标签关联成功, postId: {}, tagIds: {}", postId, tagIds);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存帖子标签关联失败, postId: {}, tagIds: {}", postId, tagIds, e);
            throw new BusinessException("保存帖子标签关联失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量获取帖子的标签ID列表
     */
    public List<PostTagService.PostTagRelation> batchGetTagIdsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            return postTagRepository.batchGetTagIdsByPostIds(postIds);
        } catch (Exception e) {
            log.error("批量获取帖子标签ID列表失败, postIds: {}", postIds, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 删除帖子的所有标签关联关系
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePostTags(Long postId) {
        if (postId == null) {
            return;
        }
        
        try {
            postTagRepository.deletePostTags(postId);
            log.info("删除帖子标签关联成功, postId: {}", postId);
        } catch (Exception e) {
            log.error("删除帖子标签关联失败, postId: {}", postId, e);
            throw new BusinessException("删除帖子标签关联失败: " + e.getMessage());
        }
    }
    
    // ==================== 标签搜索与统计 ====================
    
    /**
     * 搜索标签
     */
    public List<Tag> searchTags(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getTagList();
        }
        
        try {
            List<Tag> result = tagRepository.searchTags(keyword.trim());
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("搜索标签失败, keyword: {}", keyword, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取热门标签
     */
    public List<Tag> getHotTags(Integer limit) {
        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;
            List<Tag> result = tagRepository.getHotTags(actualLimit);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取热门标签失败, limit: {}", limit, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取热门标签（支持时间维度）
     */
    public List<Tag> getHotTagsByTimeRange(String timeRange, Integer limit) {
        try {
            String actualTimeRange = timeRange != null ? timeRange : "all";
            int actualLimit = limit != null && limit > 0 ? limit : 10;
            List<Tag> result = tagRepository.getHotTagsByTimeRange(actualTimeRange, actualLimit);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取热门标签失败, timeRange: {}, limit: {}", timeRange, limit, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取推荐标签列表
     */
    public List<Tag> getRecommendedTags() {
        try {
            // 默认返回热门标签作为推荐
            return getHotTags(20);
        } catch (Exception e) {
            log.error("获取推荐标签失败", e);
            return new ArrayList<>();
        }
    }
    
    // ==================== 内部类定义 ====================
    
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
        
        // Getters and Setters
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
        
        // Getters and Setters  
        public Long getTagId() { return tagId; }
        public void setTagId(Long tagId) { this.tagId = tagId; }
        public String getTagName() { return tagName; }
        public void setTagName(String tagName) { this.tagName = tagName; }
        public Integer getPostCount() { return postCount; }
        public void setPostCount(Integer postCount) { this.postCount = postCount; }
    }
}
