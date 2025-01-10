package cn.xu.domain.topic.repository;

import cn.xu.domain.topic.model.entity.TopicEntity;

import java.util.List;

/**
 * 话题仓储接口
 */
public interface ITopicRepository {

    /**
     * 保存话题
     *
     * @param topicEntity 话题实体
     * @return 话题ID
     */
    Long save(TopicEntity topicEntity);

    /**
     * 更新话题
     *
     * @param topicEntity 话题实体
     */
    void update(TopicEntity topicEntity);

    /**
     * 根据ID删除话题
     *
     * @param id 话题ID
     */
    void deleteById(Long id);

    /**
     * 批量删除话题
     *
     * @param ids 话题ID列表
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据ID查询话题
     *
     * @param id 话题ID
     * @return 话题实体
     */
    TopicEntity findById(Long id);

    /**
     * 查询所有话题
     *
     * @return 话题实体列表
     */
    List<TopicEntity> findAll();

    /**
     * 查询热门话题
     *
     * @param limit 限制数量
     * @return 热门话题列表
     */
    List<TopicEntity> findHotTopics(int limit);

    /**
     * 根据分类ID查询话题列表
     *
     * @param categoryId 分类ID
     * @return 话题列表
     */
    List<TopicEntity> findByCategoryId(Long categoryId);

    /**
     * 分页查询话题列表
     *
     * @param offset 偏移量
     * @param limit  每页数量
     * @return 话题列表
     */
    List<TopicEntity> findByPage(int offset, int limit);

    /**
     * 获取话题总数
     *
     * @return 话题总数
     */
    Long count();
} 