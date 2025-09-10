package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.ArticleCollectEntity;

import java.util.List;

/**
 * 文章收藏领域服务接口
 * 处理文章收藏相关的核心业务逻辑
 */
public interface IArticleCollectDomainService {
    /**
     * 收藏文章
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 文章收藏实体
     */
    ArticleCollectEntity collectArticle(Long userId, Long articleId);

    /**
     * 取消收藏文章
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     */
    void uncollectArticle(Long userId, Long articleId);

    /**
     * 检查文章是否已收藏
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 是否已收藏
     */
    boolean isArticleCollected(Long userId, Long articleId);

    /**
     * 获取用户收藏的文章数量
     *
     * @param userId 用户ID
     * @return 收藏的文章数量
     */
    int getCollectCount(Long userId);

    /**
     * 获取用户收藏的文章ID列表
     *
     * @param userId 用户ID
     * @return 收藏的文章ID列表
     */
    List<Long> getCollectArticleIds(Long userId);

    /**
     * 批量收藏文章
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @return 成功收藏的文章数量
     */
    int collectArticles(Long userId, List<Long> articleIds);

    /**
     * 批量取消收藏文章
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @return 成功取消收藏的文章数量
     */
    int uncollectArticles(Long userId, List<Long> articleIds);
}