package cn.xu.domain.post.repository;

import cn.xu.domain.post.model.entity.TopicEntity;

import java.util.List;

/**
 * 话题仓储接口
 * 负责话题数据的访问和操作
 */
public interface ITopicRepository {
    
    /**
     * 添加话题
     *
     * @param name 话题名称
     */
    void addTopic(String name);
    
    /**
     * 根据ID获取话题
     *
     * @param id 话题ID
     * @return 话题实体
     */
    TopicEntity getTopicById(Long id);
    
    /**
     * 获取所有话题
     *
     * @return 话题实体列表
     */
    List<TopicEntity> getAllTopics();
    
    /**
     * 搜索话题
     *
     * @param keyword 搜索关键词
     * @return 话题实体列表
     */
    List<TopicEntity> searchTopics(String keyword);
    
    /**
     * 获取热门话题
     *
     * @param limit 限制数量
     * @return 话题实体列表
     */
    List<TopicEntity> getHotTopics(int limit);
    
    /**
     * 根据帖子ID获取话题ID列表
     *
     * @param postId 帖子ID
     * @return 话题ID列表
     */
    List<Long> findTopicIdsByPostId(Long postId);
    
    /**
     * 根据话题ID获取帖子列表
     *
     * @param topicId 话题ID
     * @param offset 偏移量
     * @param limit 数量
     * @return 帖子列表
     */
    List<cn.xu.domain.post.model.entity.PostEntity> findPostsByTopicId(Long topicId, int offset, int limit);
}