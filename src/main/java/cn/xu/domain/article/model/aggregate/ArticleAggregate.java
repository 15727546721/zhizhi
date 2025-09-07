package cn.xu.domain.article.model.aggregate;

import cn.xu.domain.article.model.entity.ArticleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文章聚合根
 * DDD设计中的文章聚合根，包含文章实体和相关的标签关联
 * 负责维护文章与标签之间的一致性边界
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleAggregate {

    /**
     * 聚合根ID（与文章ID相同）
     */
    private Long id;
    
    /**
     * 文章实体
     */
    private ArticleEntity articleEntity;
    
    /**
     * 文章关联的标签ID列表
     */
    private List<Long> tagIds;
    
    /**
     * 发布文章聚合根
     */
    public void publish() {
        if (articleEntity != null) {
            articleEntity.publish();
        }
    }
    
    /**
     * 撤回文章聚合根
     */
    public void withdraw() {
        if (articleEntity != null) {
            articleEntity.withdraw();
        }
    }
    
    /**
     * 删除文章聚合根
     */
    public void delete() {
        if (articleEntity != null) {
            articleEntity.delete();
        }
    }
    
    /**
     * 更新文章内容
     */
    public void updateContent(String title, String content, String description) {
        if (articleEntity != null) {
            articleEntity.updateContent(title, content, description);
        }
    }
    
    /**
     * 设置标签
     */
    public void setTags(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
    
    /**
     * 获取聚合根的用户ID
     */
    public Long getUserId() {
        return articleEntity != null ? articleEntity.getUserId() : null;
    }
    
    /**
     * 验证用户权限
     */
    public void validateOwnership(Long currentUserId) {
        if (articleEntity != null) {
            articleEntity.validateOwnership(currentUserId);
        }
    }
    
    /**
     * 判断是否已发布
     */
    public boolean isPublished() {
        return articleEntity != null && articleEntity.isPublished();
    }
    
    /**
     * 验证文章创建前的完整性
     */
    public void validateForCreation() {
        if (articleEntity == null) {
            throw new IllegalStateException("文章实体不能为空");
        }
        
        // 验证文章实体的完整性
        articleEntity.validateForCreation();
        
        // 可以添加其他聚合级别的验证
        if (tagIds != null && tagIds.size() > 10) {
            throw new IllegalArgumentException("文章标签数量不能超过10个");
        }
    }
    
    /**
     * 增加文章浏览数
     */
    public void incrementViewCount() {
        if (articleEntity != null) {
            Long currentViewCount = articleEntity.getViewCount();
            articleEntity.setViewCount(currentViewCount != null ? currentViewCount + 1 : 1L);
        }
    }
    
    /**
     * 更新文章内容
     */
    public void updateArticle(ArticleEntity updatedArticle) {
        if (articleEntity != null && updatedArticle != null) {
            // 更新文章实体的内容
            articleEntity.updateContent(
                updatedArticle.getTitleValue(),
                updatedArticle.getContentValue(), 
                updatedArticle.getDescription()
            );
            // 可以根据需要更新其他字段
            if (updatedArticle.getCoverUrl() != null) {
                articleEntity.setCoverUrl(updatedArticle.getCoverUrl());
            }
            if (updatedArticle.getCategoryId() != null) {
                articleEntity.setCategoryId(updatedArticle.getCategoryId());
            }
        }
    }
}
