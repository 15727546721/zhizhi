package cn.xu.domain.topic.service;

import cn.xu.domain.topic.command.CreateTopicCategoryCommand;
import cn.xu.domain.topic.model.entity.TopicCategoryEntity;

import java.util.List;

/**
 * 话题分类服务接口
 */
public interface ITopicCategoryService {
    
    /**
     * 创建分类
     *
     * @param command 创建分类命令
     * @return 分类实体
     */
    TopicCategoryEntity createCategory(CreateTopicCategoryCommand command);
    
    /**
     * 更新分类
     *
     * @param id 分类ID
     * @param command 创建分类命令
     */
    void updateCategory(Long id, CreateTopicCategoryCommand command);
    
    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);
    
    /**
     * 获取分类
     *
     * @param id 分类ID
     * @return 分类实体
     */
    TopicCategoryEntity getCategory(Long id);
    
    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<TopicCategoryEntity> getAllCategories();
} 