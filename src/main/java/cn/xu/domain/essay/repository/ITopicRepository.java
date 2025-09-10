package cn.xu.domain.essay.repository;

import cn.xu.api.web.model.dto.essay.TopicQueryRequest;
import cn.xu.domain.essay.model.entity.TopicEntity;

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
     * 根据ID查询话题
     *
     * @param id 话题ID
     * @return 话题实体
     */
    TopicEntity findById(Long id);

    /**
     * 查询所有话题
     *
     * @return 话题列表
     */
    List<TopicEntity> findAll();

    /**
     * 根据名称查询话题
     *
     * @param name 话题名称
     * @return 话题实体
     */
    TopicEntity findByName(String name);

    /**
     * 分页查询话题
     * @param request
     * @return
     */
    List<TopicEntity> getPageByName(TopicQueryRequest request);
}
