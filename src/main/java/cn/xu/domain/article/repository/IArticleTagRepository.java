package cn.xu.domain.article.repository;

import cn.xu.infrastructure.persistent.po.ArticleTag;

import java.util.List;

public interface IArticleTagRepository {
    void save(ArticleTag articleTag);

    void saveArticleTag(Long articleId, List<Long> tagIds);
}
