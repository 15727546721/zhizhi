package cn.xu.domain.post.service;

import cn.xu.domain.post.model.entity.TagEntity;

import java.util.List;

/**
 * 帖子标签服务接口
 */
public interface IPostTagService {
    
    /**
     * 获取所有标签列表
     * 
     * @return 标签列表
     */
    List<TagEntity> getTagList();
    
    /**
     * 根据ID获取标签
     * 
     * @param id 标签ID
     * @return 标签实体
     */
    TagEntity getTagById(Long id);
    
    /**
     * 创建标签
     * 
     * @param name 标签名称
     * @return 标签实体
     */
    TagEntity createTag(String name);
    
    /**
     * 更新标签
     * 
     * @param id 标签ID
     * @param name 标签名称
     * @return 标签实体
     */
    TagEntity updateTag(Long id, String name);
    
    /**
     * 删除标签
     * 
     * @param id 标签ID
     */
    void deleteTag(Long id);
    
    /**
     * 根据帖子ID获取标签列表
     * 
     * @param postId 帖子ID
     * @return 标签列表
     */
    List<TagEntity> getTagsByPostId(Long postId);
    
    /**
     * 保存帖子标签关联关系
     * 
     * @param postId 帖子ID
     * @param tagIds 标签ID列表
     */
    void savePostTags(Long postId, List<Long> tagIds);
    
    /**
     * 批量获取帖子的标签ID列表
     *
     * @param postIds 帖子ID列表
     * @return 帖子与标签的关联关系列表
     */
    List<PostTagRelation> batchGetTagIdsByPostIds(List<Long> postIds);
    
    /**
     * 搜索标签
     * 
     * @param keyword 搜索关键词
     * @return 标签列表
     */
    List<TagEntity> searchTags(String keyword);
    
    /**
     * 获取热门标签
     * 
     * @param limit 返回数量限制
     * @return 热门标签列表
     */
    List<TagEntity> getHotTags(Integer limit);
    
    /**
     * 获取热门标签（支持时间维度）
     * 
     * @param timeRange 时间范围：today(今日)、week(本周)、month(本月)、all(全部)
     * @param limit 返回数量限制
     * @return 热门标签列表
     */
    List<TagEntity> getHotTagsByTimeRange(String timeRange, Integer limit);
    
    /**
     * 帖子与标签的关联关系
     */
    class PostTagRelation {
        private Long postId;
        private List<Long> tagIds;
        
        public PostTagRelation() {}
        
        public PostTagRelation(Long postId, List<Long> tagIds) {
            this.postId = postId;
            this.tagIds = tagIds;
        }
        
        public Long getPostId() {
            return postId;
        }
        
        public void setPostId(Long postId) {
            this.postId = postId;
        }
        
        public List<Long> getTagIds() {
            return tagIds;
        }
        
        public void setTagIds(List<Long> tagIds) {
            this.tagIds = tagIds;
        }
    }
    
    /**
     * 标签统计信息
     */
    class TagStatistics {
        private Long tagId;
        private String tagName;
        private Integer postCount;
        
        public TagStatistics() {}
        
        public TagStatistics(Long tagId, String tagName, Integer postCount) {
            this.tagId = tagId;
            this.tagName = tagName;
            this.postCount = postCount;
        }
        
        public Long getTagId() {
            return tagId;
        }
        
        public void setTagId(Long tagId) {
            this.tagId = tagId;
        }
        
        public String getTagName() {
            return tagName;
        }
        
        public void setTagName(String tagName) {
            this.tagName = tagName;
        }
        
        public Integer getPostCount() {
            return postCount;
        }
        
        public void setPostCount(Integer postCount) {
            this.postCount = postCount;
        }
    }
}