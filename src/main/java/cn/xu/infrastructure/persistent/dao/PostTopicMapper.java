package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.PostTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostTopicMapper {
    void insert(PostTopic postTopic);

    void insertBatchByList(List<PostTopic> postTopics);

    void deleteByPostId(@Param("postId") Long postId);
    
    // 根据帖子ID和话题ID列表删除话题关联
    void deleteByPostIdAndTopicIds(@Param("postId") Long postId, @Param("topicIds") List<Long> topicIds);
    
    // 添加根据帖子ID查询话题ID的方法
    List<Long> selectTopicIdsByPostId(@Param("postId") Long postId);
    
    // 根据话题ID查询帖子ID列表
    List<Long> selectPostIdsByTopicId(@Param("topicId") Long topicId, @Param("offset") int offset, @Param("limit") int limit);
    
    // 根据话题ID统计帖子数量
    Long countPostsByTopicId(@Param("topicId") Long topicId);
    
    // 根据帖子ID列表查询话题关联信息
    List<PostTopic> selectByPostIds(@Param("postIds") List<Long> postIds);
    
    // 根据用户ID获取话题统计信息（话题ID、参与次数、最后参与时间）
    List<cn.xu.infrastructure.persistent.dao.PostTopicMapper.UserTopicStats> selectTopicStatsByUserId(
        @Param("userId") Long userId, 
        @Param("offset") int offset, 
        @Param("limit") int limit
    );
    
    // 根据用户ID统计话题数量
    Long countTopicsByUserId(@Param("userId") Long userId);
    
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
}