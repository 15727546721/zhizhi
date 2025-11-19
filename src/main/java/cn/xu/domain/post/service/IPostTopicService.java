package cn.xu.domain.post.service;

import java.util.List;

/**
 * 帖子话题服务接口
 */
public interface IPostTopicService {
    
    /**
     * 保存帖子话题关联关系
     * 
     * @param postId 帖子ID
     * @param topicIds 话题ID列表
     */
    void savePostTopics(Long postId, List<Long> topicIds);
    
    /**
     * 根据帖子ID获取话题列表
     * 
     * @param postId 帖子ID
     * @return 话题ID列表
     */
    List<Long> getTopicsByPostId(Long postId);
    
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
     * 统计指定话题下的帖子数量
     *
     * @param topicId 话题ID
     * @return 帖子数量
     */
    Long countPostsByTopicId(Long topicId);
    
    /**
     * 批量获取帖子的关联话题ID
     *
     * @param postIds 帖子ID列表
     * @return 帖子与话题的关联关系列表
     */
    List<PostTopicRelation> batchGetTopicIdsByPostIds(List<Long> postIds);
    
    /**
     * 帖子与话题的关联关系
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    class PostTopicRelation {
        private Long postId;
        private List<Long> topicIds;
    }
}