package cn.xu.domain.article.service.impl;

import cn.xu.domain.article.repository.IArticleCategoryRepository;
import cn.xu.domain.article.service.IArticleCategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ArticleCategoryService implements IArticleCategoryService {

    @Resource
    private IArticleCategoryRepository articleCategoryRepository;

    @Override
    public void saveArticleCategory(Long articleId, Long categoryId) {
        articleCategoryRepository.saveArticleCategory(articleId, categoryId);
    }

    @Override
    public void updateArticleCategory(Long articleId, Long categoryId) {
        articleCategoryRepository.deleteArticleCategory(articleId);
        articleCategoryRepository.saveArticleCategory(articleId, categoryId);
    }
}
