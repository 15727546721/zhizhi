package cn.xu.domain.article.repository;

import cn.xu.infrastructure.persistent.po.ArticleTag;

public interface IArticleTagRepository {
    void save(ArticleTag articleTag);
}
