package cn.xu.domain.post.service;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;

import java.util.List;

/**
 * 帖子分类服务接口
 * 专门处理帖子与分类的关联关系
 */
public interface IPostCategoryService {
    
    /**
     * 根据分类ID获取帖子列表
     *
     * @param categoryId 分类ID
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 帖子列表
     */
    List<PostEntity> getPostsByCategoryId(Long categoryId, Integer pageNo, Integer pageSize);
    
    /**
     * 获取指定类型的热门分类列表
     *
     * @param postType 帖子类型
     * @param limit 限制数量
     * @return 热门分类列表
     */
    List<CategoryInfo> getHotCategoriesByType(PostType postType, int limit);
    
    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    List<CategoryInfo> getAllCategories();
    
    /**
     * 根据分类名称获取分类信息
     *
     * @param categoryName 分类名称
     * @return 分类信息
     */
    CategoryInfo getCategoryByName(String categoryName);
    
    /**
     * 获取推荐分类列表
     *
     * @return 推荐分类列表
     */
    List<CategoryInfo> getRecommendedCategories();
    
    /**
     * 分类信息数据传输对象
     */
    class CategoryInfo {
        private Long id;
        private String name;
        private String description;
        private Long postCount;
        private PostType postType; // 该分类主要支持的帖子类型
        
        public CategoryInfo() {}
        
        public CategoryInfo(Long id, String name, String description, Long postCount, PostType postType) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.postCount = postCount;
            this.postType = postType;
        }
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public Long getPostCount() {
            return postCount;
        }
        
        public void setPostCount(Long postCount) {
            this.postCount = postCount;
        }
        
        public PostType getPostType() {
            return postType;
        }
        
        public void setPostType(PostType postType) {
            this.postType = postType;
        }
    }
}