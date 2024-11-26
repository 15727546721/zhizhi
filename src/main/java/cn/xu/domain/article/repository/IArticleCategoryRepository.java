package cn.xu.domain.article.repository;

public interface IArticleCategoryRepository {

    void saveArticleCategory(Long articleId, Long categoryId);

    void deleteArticleCategory(Long articleId);
}
