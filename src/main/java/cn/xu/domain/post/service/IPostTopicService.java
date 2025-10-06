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
}