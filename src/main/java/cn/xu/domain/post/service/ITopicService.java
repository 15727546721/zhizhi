package cn.xu.domain.post.service;

import cn.xu.domain.post.model.entity.TopicEntity;

import java.util.List;

/**
 * 话题服务接口
 * 负责话题相关的业务逻辑处理
 */
public interface ITopicService {
    
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
     * 批量获取话题
     *
     * @param ids 话题ID列表
     * @return 话题实体列表
     */
    List<TopicEntity> batchGetTopics(List<Long> ids);
}