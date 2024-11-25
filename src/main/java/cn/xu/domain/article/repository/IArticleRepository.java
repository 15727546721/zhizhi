package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.infrastructure.persistent.po.Article;

import java.util.List;

public interface IArticleRepository {
    Long save(ArticleEntity articleAggregate);

    List<ArticleEntity> queryArticle(int page, int size);

    void deleteByIds(List<Long> articleIds);

    Article findById(Long id);
}
