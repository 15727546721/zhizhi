package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.ArticleCollectEntity;
import cn.xu.domain.article.model.entity.CollectFolderEntity;

import java.util.List;

/**
 * 文章收藏编排服务接口
 * 统一协调文章收藏和收藏夹功能，遵循DDD原则
 */
public interface IArticleCollectionOrchestrationService {

    /**
     * 收藏文章到默认收藏夹
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 文章收藏实体
     */
    ArticleCollectEntity collectArticleToDefaultFolder(Long userId, Long articleId);

    /**
     * 收藏文章到指定收藏夹
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @param folderId  收藏夹ID
     * @return 文章收藏实体
     */
    ArticleCollectEntity collectArticleToFolder(Long userId, Long articleId, Long folderId);

    /**
     * 取消收藏文章
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     */
    void uncollectArticle(Long userId, Long articleId);

    /**
     * 批量收藏文章到默认收藏夹
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @return 成功收藏的文章数量
     */
    int collectArticlesToDefaultFolder(Long userId, List<Long> articleIds);

    /**
     * 批量收藏文章到指定收藏夹
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @param folderId   收藏夹ID
     * @return 成功收藏的文章数量
     */
    int collectArticlesToFolder(Long userId, List<Long> articleIds, Long folderId);

    /**
     * 批量取消收藏文章
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @return 成功取消收藏的文章数量
     */
    int uncollectArticles(Long userId, List<Long> articleIds);

    /**
     * 获取用户默认收藏夹
     *
     * @param userId 用户ID
     * @return 默认收藏夹
     */
    CollectFolderEntity getUserDefaultFolder(Long userId);

    /**
     * 获取用户的收藏夹列表
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    List<CollectFolderEntity> getUserFolders(Long userId);

    /**
     * 检查文章是否已收藏
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 是否已收藏
     */
    boolean isArticleCollected(Long userId, Long articleId);

    /**
     * 获取用户收藏的文章ID列表
     *
     * @param userId 用户ID
     * @return 收藏的文章ID列表
     */
    List<Long> getCollectArticleIds(Long userId);
}