package cn.xu.domain.article.repository;

import cn.xu.infrastructure.persistent.po.ArticleTagRelation;

import java.util.List;

public interface IArticleTagRepository {
    void save(ArticleTagRelation articleTagRelation);

    void saveArticleTag(Long articleId, List<Long> tagIds);

    void deleteByArticleId(Long articleId);
}
