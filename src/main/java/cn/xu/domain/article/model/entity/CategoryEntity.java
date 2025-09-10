package cn.xu.domain.article.model.entity;

import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分类领域实体
 * 封装文章分类相关的业务逻辑和规则
 * 
 * @author xu
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CategoryEntity {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 文章数量（可选）
    private Long articleCount;
    
    // ==================== 业务方法 ====================
    
    /**
     * 创建新分类
     */
    public static CategoryEntity create(String name) {
        return create(name, null);
    }
    
    /**
     * 创建新分类（带描述）
     */
    public static CategoryEntity create(String name, String description) {
        validateCategoryName(name);
        
        return CategoryEntity.builder()
                .name(name.trim())
                .description(description != null ? description.trim() : null)
                .articleCount(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 更新分类信息
     */
    public void updateInfo(String newName, String newDescription) {
        validateCategoryName(newName);
        
        boolean changed = false;
        
        if (!this.name.equals(newName.trim())) {
            this.name = newName.trim();
            changed = true;
        }
        
        String trimmedDesc = newDescription != null ? newDescription.trim() : null;
        if (!java.util.Objects.equals(this.description, trimmedDesc)) {
            this.description = trimmedDesc;
            changed = true;
        }
        
        if (changed) {
            this.updateTime = LocalDateTime.now();
        }
    }
    
    /**
     * 增加文章数量
     */
    public void increaseArticleCount() {
        this.articleCount = (this.articleCount == null ? 0 : this.articleCount) + 1;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 减少文章数量
     */
    public void decreaseArticleCount() {
        this.articleCount = Math.max(0, (this.articleCount == null ? 0 : this.articleCount) - 1);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 判断分类是否为空（没有文章）
     */
    public boolean isEmpty() {
        return this.articleCount == null || this.articleCount == 0;
    }
    
    /**
     * 判断分类是否是热门分类
     */
    public boolean isPopular() {
        // 简单判断：文章数量超过50认为是热门分类
        return this.articleCount != null && this.articleCount >= 50;
    }
    
    /**
     * 判断分类名称是否相同
     */
    public boolean hasSameName(String otherName) {
        if (otherName == null || this.name == null) {
            return false;
        }
        return this.name.equalsIgnoreCase(otherName.trim());
    }
    
    /**
     * 判断分类是否刚创建
     */
    public boolean isNewlyCreated() {
        if (createTime == null) {
            return false;
        }
        return java.time.Duration.between(createTime, LocalDateTime.now()).toHours() < 24;
    }
    
    /**
     * 获取分类的显示信息
     */
    public String getDisplayInfo() {
        long count = this.articleCount != null ? this.articleCount : 0;
        return String.format("%s (%d篇文章)", this.name, count);
    }
    
    /**
     * 验证分类名称
     */
    public static void validateCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("分类名称不能为空");
        }
        
        String trimmedName = name.trim();
        
        if (trimmedName.length() > 50) {
            throw new BusinessException("分类名称不能超过50个字符");
        }
        
        if (trimmedName.length() < 2) {
            throw new BusinessException("分类名称不能少于2个字符");
        }
        
        // 禁止特殊字符
        if (trimmedName.matches(".*[<>\"'&]")) {
            throw new BusinessException("分类名称不能包含特殊字符");
        }
    }
    
    /**
     * 获取分类的简化信息（用于日志）
     */
    public String getSimpleInfo() {
        return String.format("Category[%d:%s]", id, name);
    }
    
    /**
     * 判断两个分类是否相同（按名称匹配）
     */
    public boolean isSameCategory(CategoryEntity other) {
        if (other == null) {
            return false;
        }
        return hasSameName(other.getName());
    }
    
    /**
     * 重置文章数量（用于重新统计）
     */
    public void resetArticleCount() {
        this.articleCount = 0L;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 设置文章数量（直接设置）
     */
    public void setArticleCount(Long count) {
        if (count == null || count < 0) {
            throw new BusinessException("文章数量不能为负数");
        }
        this.articleCount = count;
        this.updateTime = LocalDateTime.now();
    }
}
