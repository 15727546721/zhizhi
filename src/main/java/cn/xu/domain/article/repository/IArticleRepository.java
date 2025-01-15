package cn.xu.domain.article.repository;

import cn.xu.api.controller.web.article.ArticleListDTO;
import cn.xu.api.dto.article.ArticlePageResponse;
import cn.xu.api.dto.article.ArticleRequest;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.ArticleRecommendOrNew;

import java.util.List;
import java.util.Map;

public interface IArticleRepository {
    Long save(ArticleEntity articleAggregate);

    List<ArticlePageResponse> queryArticle(ArticleRequest articleRequest);

    void deleteByIds(List<Long> articleIds);

    ArticleEntity findById(Long id);

    void update(ArticleEntity articleEntity);

    List<ArticleRecommendOrNew> queryArticleByPage();

    List<ArticleListDTO> queryArticleByCategory(Long categoryId);

    /**
     * 更新文章点赞数
     *
     * @param articleId 文章ID
     * @param likeCount 点赞数
     */
    void updateArticleLikeCount(Long articleId, Long likeCount);

    /**
     * 批量更新文章点赞数
     *
     * @param likeCounts key为文章ID，value为点赞数的Map
     */
    void batchUpdateArticleLikeCount(Map<Long, Long> likeCounts);

    /**
     * 获取所有文章
     *
     * @return 所有文章列表
     */
    List<ArticleEntity> findAll();
}
