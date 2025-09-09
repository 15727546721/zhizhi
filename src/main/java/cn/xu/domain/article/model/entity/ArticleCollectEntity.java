package cn.xu.domain.article.model.entity;

import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章收藏领域实体
 * 封装文章收藏相关的业务逻辑和规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCollectEntity {
    private Long id;
    private Long userId;
    private Long articleId;
    private int status; // 0: 未收藏, 1: 已收藏
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 创建文章收藏记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 文章收藏实体
     */
    public static ArticleCollectEntity create(Long userId, Long articleId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        if (articleId == null || articleId <= 0) {
            throw new BusinessException("文章ID不能为空");
        }

        return ArticleCollectEntity.builder()
                .userId(userId)
                .articleId(articleId)
                .status(1) // 默认为已收藏状态
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 收藏文章
     */
    public void collect() {
        validateForOperation();
        this.status = 1;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 取消收藏文章
     */
    public void uncollect() {
        validateForOperation();
        this.status = 0;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 检查是否已收藏
     *
     * @return 是否已收藏
     */
    public boolean isCollected() {
        return this.status == 1;
    }

    /**
     * 验证实体有效性
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return userId != null && userId > 0 &&
               articleId != null && articleId > 0 &&
               (status == 0 || status == 1);
    }

    /**
     * 验证操作前的实体状态
     */
    private void validateForOperation() {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        if (articleId == null || articleId <= 0) {
            throw new BusinessException("文章ID不能为空");
        }
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getStatusDescription() {
        switch (status) {
            case 0: return "未收藏";
            case 1: return "已收藏";
            default: return "未知状态";
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
}