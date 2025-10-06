package cn.xu.domain.post.repository;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.service.IPostCategoryService;

import java.util.List;

/**
 * 帖子分类仓储接口
 * 负责帖子与分类关联关系的数据访问
 */
public interface IPostCategoryRepository {
    
    /**
     * 根据分类ID获取帖子列表
     *
     * @param categoryId 分类ID
     * @param offset 偏移量
     * @param limit 数量
     * @return 帖子列表
     */
    List<PostEntity> getPostsByCategoryId(Long categoryId, int offset, int limit);
    
    /**
     * 获取指定类型的热门分类列表
     *
     * @param postType 帖子类型
     * @param limit 限制数量
     * @return 热门分类列表
     */
    List<IPostCategoryService.CategoryInfo> getHotCategoriesByType(PostType postType, int limit);
    
    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    List<IPostCategoryService.CategoryInfo> getAllCategories();
    
    /**
     * 根据分类名称获取分类信息
     *
     * @param categoryName 分类名称
     * @return 分类信息
     */
    IPostCategoryService.CategoryInfo getCategoryByName(String categoryName);
    
    /**
     * 获取推荐分类列表
     *
     * @return 推荐分类列表
     */
    List<IPostCategoryService.CategoryInfo> getRecommendedCategories();
}