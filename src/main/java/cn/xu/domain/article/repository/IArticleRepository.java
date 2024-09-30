package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.aggregate.ArticleAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;

import java.util.List;

public interface IArticleRepository {
    void save(ArticleAggregate articleAggregate);

    List<ArticleEntity> queryArticle(int page, int size);
}
