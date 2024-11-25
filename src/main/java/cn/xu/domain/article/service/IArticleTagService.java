package cn.xu.domain.article.service;

import java.util.List;

public interface IArticleTagService {
    void saveArticleTag(Long articleId, List<Long> tagIds);
}
