package cn.xu.domain.article.repository;

import cn.xu.api.dto.article.ArticlePageResponse;
import cn.xu.api.dto.article.ArticleRequest;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.ArticleRecommendOrNew;

import java.util.List;

public interface IArticleRepository {
    Long save(ArticleEntity articleAggregate);

    List<ArticlePageResponse> queryArticle(ArticleRequest articleRequest);

    void deleteByIds(List<Long> articleIds);

    ArticleEntity findById(Long id);

    void update(ArticleEntity articleEntity);

    List<ArticleRecommendOrNew> queryArticleByPage();
}
