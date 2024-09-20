package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.entity.ArticleEntity;

public interface IArticleRepository {
    void save(ArticleEntity articleEntity);
}
