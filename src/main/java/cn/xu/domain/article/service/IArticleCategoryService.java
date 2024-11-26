package cn.xu.domain.article.service;

public interface IArticleCategoryService {
    void saveArticleCategory(Long articleId, Long categoryId);

    void updateArticleCategory(Long articleId, Long categoryId);
}
