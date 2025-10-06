package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.PostCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子分类关联数据访问接口
 */
@Mapper
public interface PostCategoryMapper {
    
    /**
     * 根据分类ID查询帖子ID列表
     *
     * @param categoryId 分类ID
     * @param offset 偏移量
     * @param limit 数量
     * @return 帖子ID列表
     */
    List<Long> selectPostIdsByCategoryId(@Param("categoryId") Long categoryId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计分类下的帖子数量
     *
     * @param categoryId 分类ID
     * @return 帖子数量
     */
    int countByCategoryId(Long categoryId);
    
    /**
     * 获取热门分类列表
     *
     * @param postType 帖子类型
     * @param limit 限制数量
     * @return 分类使用统计列表
     */
    List<CategoryStatistics> selectHotCategories(@Param("postType") String postType, @Param("limit") int limit);
    
    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    List<PostCategory> selectAllCategories();
    
    /**
     * 根据分类名称查询分类信息
     *
     * @param categoryName 分类名称
     * @return 分类信息
     */
    PostCategory selectByCategoryName(String categoryName);
    
    /**
     * 分类统计信息结果映射类
     */
    class CategoryStatistics {
        private Long categoryId;
        private String categoryName;
        private String categoryDescription;
        private Integer postCount;
        
        // Getters and Setters
        public Long getCategoryId() {
            return categoryId;
        }
        
        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }
        
        public String getCategoryName() {
            return categoryName;
        }
        
        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
        
        public String getCategoryDescription() {
            return categoryDescription;
        }
        
        public void setCategoryDescription(String categoryDescription) {
            this.categoryDescription = categoryDescription;
        }
        
        public Integer getPostCount() {
            return postCount;
        }
        
        public void setPostCount(Integer postCount) {
            this.postCount = postCount;
        }
    }
}