package cn.xu.domain.post.repository;

import java.util.List;

/**
 * 帖子话题仓储接口
 * 负责帖子与话题关联关系的数据访问
 */
public interface IPostTopicRepository {
    
    /**
     * 保存帖子话题关联关系
     *
     * @param postId 帖子ID
     * @param topicIds 话题ID列表
     */
    void savePostTopic(Long postId, List<Long> topicIds);
    
    /**
     * 根据帖子ID获取话题ID列表
     *
     * @param postId 帖子ID
     * @return 话题ID列表
     */
    List<Long> getTopicIdsByPostId(Long postId);
    
    /**
     * 根据话题ID获取帖子列表
     *
     * @param topicId 话题ID
     * @param offset 偏移量
     * @param limit 数量
     * @return 帖子ID列表
     */
    List<Long> getPostIdsByTopicId(Long topicId, int offset, int limit);
    
    /**
     * 批量获取帖子的话题ID列表
     *
     * @param postIds 帖子ID列表
     * @return 帖子与话题的关联关系列表
     */
    List<PostTopicRelation> batchGetTopicIdsByPostIds(List<Long> postIds);
    
    /**
     * 删除帖子的所有话题关联关系
     *
     * @param postId 帖子ID
     */
    void deletePostTopics(Long postId);
    
    /**
     * 根据用户ID获取话题统计信息
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 数量
     * @return 话题统计信息列表
     */
    List<UserTopicStats> getTopicStatsByUserId(Long userId, int offset, int limit);
    
    /**
     * 根据用户ID统计话题数量
     *
     * @param userId 用户ID
     * @return 话题数量
     */
    Long countTopicsByUserId(Long userId);
    
    /**
     * 用户话题统计信息
     */
    class UserTopicStats {
        private Long topicId;
        private Long postCount;
        private java.time.LocalDateTime lastPostTime;
        
        public UserTopicStats() {}
        
        public Long getTopicId() {
            return topicId;
        }
        
        public void setTopicId(Long topicId) {
            this.topicId = topicId;
        }
        
        public Long getPostCount() {
            return postCount;
        }
        
        public void setPostCount(Long postCount) {
            this.postCount = postCount;
        }
        
        public java.time.LocalDateTime getLastPostTime() {
            return lastPostTime;
        }
        
        public void setLastPostTime(java.time.LocalDateTime lastPostTime) {
            this.lastPostTime = lastPostTime;
        }
    }
    
    /**
     * 帖子与话题的关联关系
     */
    class PostTopicRelation {
        private Long postId;
        private List<Long> topicIds;
        
        public PostTopicRelation() {}
        
        public PostTopicRelation(Long postId, List<Long> topicIds) {
            this.postId = postId;
            this.topicIds = topicIds;
        }
        
        public Long getPostId() {
            return postId;
        }
        
        public void setPostId(Long postId) {
            this.postId = postId;
        }
        
        public List<Long> getTopicIds() {
            return topicIds;
        }
        
        public void setTopicIds(List<Long> topicIds) {
            this.topicIds = topicIds;
        }
    }
}