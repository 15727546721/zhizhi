package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.entity.ArticleEntity;

import java.util.List;

public interface IArticleRepository {
    void save(ArticleEntity articleEntity);

    List<ArticleEntity> queryArticle(int page, int size);
}
