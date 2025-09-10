package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.entity.CollectFolderArticleEntity;

import java.util.List;
import java.util.Optional;

/**
 * 收藏夹文章关联仓储接口
 * 遵循DDD原则，只处理收藏夹文章关联领域实体的操作
 */
public interface ICollectFolderArticleRepository {
    /**
     * 保存收藏夹文章关联记录
     *
     * @param collectFolderArticleEntity 收藏夹文章关联实体
     * @return 关联记录ID
     */
    Long save(CollectFolderArticleEntity collectFolderArticleEntity);

    /**
     * 删除收藏夹文章关联记录
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     */
    void deleteByFolderIdAndArticleId(Long folderId, Long articleId);

    /**
     * 根据收藏夹ID删除关联记录
     *
     * @param folderId 收藏夹ID
     */
    void deleteByFolderId(Long folderId);

    /**
     * 根据文章ID删除关联记录
     *
     * @param articleId 文章ID
     */
    void deleteByArticleId(Long articleId);

    /**
     * 根据收藏夹ID查找关联记录列表
     *
     * @param folderId 收藏夹ID
     * @return 关联记录列表
     */
    List<CollectFolderArticleEntity> findByFolderId(Long folderId);

    /**
     * 根据用户ID和文章ID查找关联记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 关联记录列表
     */
    List<CollectFolderArticleEntity> findByUserIdAndArticleId(Long userId, Long articleId);

    /**
     * 根据收藏夹ID和文章ID查找关联记录
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @return 关联记录
     */
    Optional<CollectFolderArticleEntity> findByFolderIdAndArticleId(Long folderId, Long articleId);

    /**
     * 检查文章是否已被收藏到指定收藏夹
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @return 是否已收藏
     */
    boolean existsByFolderIdAndArticleId(Long folderId, Long articleId);

    /**
     * 统计收藏夹中的文章数量
     *
     * @param folderId 收藏夹ID
     * @return 文章数量
     */
    int countByFolderId(Long folderId);
}