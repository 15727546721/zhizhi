package cn.xu.domain.article.model.entity;

import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏夹文章关联实体
 * 封装收藏夹与文章关联的业务逻辑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectFolderArticleEntity {
    private Long id;
    private Long folderId;
    private Long articleId;
    private Long userId;
    private LocalDateTime createTime;

    /**
     * 创建收藏夹文章关联记录
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 收藏夹文章关联实体
     */
    public static CollectFolderArticleEntity createRelation(Long folderId, Long articleId, Long userId) {
        if (folderId == null || folderId <= 0) {
            throw new BusinessException("收藏夹ID不能为空");
        }
        if (articleId == null || articleId <= 0) {
            throw new BusinessException("文章ID不能为空");
        }
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }

        return CollectFolderArticleEntity.builder()
                .folderId(folderId)
                .articleId(articleId)
                .userId(userId)
                .createTime(LocalDateTime.now())
                .build();
    }

    /**
     * 验证关联记录的有效性
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return folderId != null && folderId > 0 &&
               articleId != null && articleId > 0 &&
               userId != null && userId > 0;
    }
}