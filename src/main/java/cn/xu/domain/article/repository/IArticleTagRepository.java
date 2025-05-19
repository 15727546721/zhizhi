package cn.xu.domain.article.repository;

import cn.xu.infrastructure.persistent.po.ArticleTagRel;

import java.util.List;

public interface IArticleTagRepository {
    void save(ArticleTagRel articleTagRel);

    void saveArticleTag(Long articleId, List<Long> tagIds);

    void deleteByArticleId(Long articleId);
}
