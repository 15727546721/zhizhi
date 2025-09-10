package cn.xu.domain.article.model.entity;

import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏夹领域实体
 * 封装收藏夹相关的业务逻辑和规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectFolderEntity {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private Boolean isDefault;
    private Integer articleCount;
    private Boolean isPublic;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 创建新的收藏夹
     *
     * @param userId      用户ID
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     * @param isPublic    是否公开
     * @return 收藏夹实体
     */
    public static CollectFolderEntity createFolder(Long userId, String name, String description, Boolean isPublic) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("收藏夹名称不能为空");
        }
        if (name.length() > 100) {
            throw new BusinessException("收藏夹名称不能超过100个字符");
        }

        return CollectFolderEntity.builder()
                .userId(userId)
                .name(name.trim())
                .description(description)
                .isDefault(false)
                .articleCount(0)
                .isPublic(isPublic != null ? isPublic : false)
                .sort(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 更新收藏夹信息
     *
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     * @param isPublic    是否公开
     */
    public void updateFolderInfo(String name, String description, Boolean isPublic) {
        if (name != null && !name.trim().isEmpty()) {
            if (name.length() > 100) {
                throw new BusinessException("收藏夹名称不能超过100个字符");
            }
            this.name = name.trim();
        }

        this.description = description;
        if (isPublic != null) {
            this.isPublic = isPublic;
        }
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置为默认收藏夹
     */
    public void setAsDefault() {
        this.isDefault = true;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 取消默认收藏夹
     */
    public void unsetAsDefault() {
        this.isDefault = false;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 增加文章数量
     */
    public void incrementArticleCount() {
        this.articleCount = (this.articleCount == null ? 0 : this.articleCount) + 1;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 减少文章数量
     */
    public void decrementArticleCount() {
        this.articleCount = Math.max(0, (this.articleCount == null ? 0 : this.articleCount) - 1);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置文章数量
     *
     * @param count 文章数量
     */
    public void setArticleCount(Integer count) {
        if (count == null || count < 0) {
            throw new BusinessException("文章数量不能为负数");
        }
        this.articleCount = count;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新排序
     *
     * @param sort 排序值
     */
    public void updateSort(Integer sort) {
        if (sort == null) {
            throw new BusinessException("排序值不能为空");
        }
        this.sort = sort;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 验证是否可以删除
     *
     * @return 是否可以删除
     */
    public boolean canDelete() {
        // 默认收藏夹不能删除
        if (Boolean.TRUE.equals(this.isDefault)) {
            return false;
        }
        // 有文章的收藏夹不能删除
        if (this.articleCount != null && this.articleCount > 0) {
            return false;
        }
        return true;
    }

    /**
     * 验证收藏夹ID
     *
     * @param folderId 收藏夹ID
     */
    public static void validateFolderId(Long folderId) {
        if (folderId == null || folderId <= 0) {
            throw new BusinessException("收藏夹ID不能为空");
        }
    }

    /**
     * 验证用户ID
     *
     * @param userId 用户ID
     */
    public static void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
    }

    /**
     * 验证文章ID
     *
     * @param articleId 文章ID
     */
    public static void validateArticleId(Long articleId) {
        if (articleId == null || articleId <= 0) {
            throw new BusinessException("文章ID不能为空");
        }
    }
}