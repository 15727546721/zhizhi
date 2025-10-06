package cn.xu.domain.post.service;

import cn.xu.domain.post.model.entity.CategoryEntity;

import java.util.List;

/**
 * 分类服务接口
 * 负责分类相关的业务逻辑处理
 */
public interface ICategoryService {
    
    /**
     * 保存分类
     *
     * @param category 分类实体
     */
    void save(CategoryEntity category);
    
    /**
     * 分页查询分类列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 分类列表
     */
    List<CategoryEntity> queryCategoryList(int page, int size);
    
    /**
     * 更新分类
     *
     * @param categoryEntity 分类实体
     */
    void update(CategoryEntity categoryEntity);
    
    /**
     * 删除分类
     *
     * @param idList 分类ID列表
     */
    void delete(List<Long> idList);
    
    /**
     * 获取分类选择列表
     *
     * @return 分类列表
     */
    List<CategoryEntity> getCategorySelect();
    
    /**
     * 根据帖子ID获取分类
     *
     * @param id 帖子ID
     * @return 分类实体
     */
    CategoryEntity getCategoryByPostId(Long id);
    
    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    List<CategoryEntity> getCategoryList();
    
    /**
     * 搜索分类
     *
     * @param keyword 搜索关键词
     * @return 分类列表
     */
    List<CategoryEntity> searchCategories(String keyword);
}