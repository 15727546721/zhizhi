package cn.xu.domain.topic.repository;

import cn.xu.domain.topic.model.entity.TopicCategoryEntity;

import java.util.List;

/**
 * 话题分类仓储接口
 */
public interface ITopicCategoryRepository {

    /**
     * 保存分类
     *
     * @param categoryEntity 分类实体
     * @return 分类ID
     */
    Long save(TopicCategoryEntity categoryEntity);

    /**
     * 更新分类
     *
     * @param categoryEntity 分类实体
     */
    void update(TopicCategoryEntity categoryEntity);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteById(Long id);

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类实体
     */
    TopicCategoryEntity findById(Long id);

    /**
     * 查询所有分类
     *
     * @return 分类列表
     */
    List<TopicCategoryEntity> findAll();

    /**
     * 根据名称查询分类
     *
     * @param name 分类名称
     * @return 分类实体
     */
    TopicCategoryEntity findByName(String name);
} 