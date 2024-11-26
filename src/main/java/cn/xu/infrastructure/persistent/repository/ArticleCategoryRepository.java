package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.repository.IArticleCategoryRepository;
import cn.xu.infrastructure.persistent.dao.IArticleCategoryDao;
import cn.xu.infrastructure.persistent.po.ArticleCategory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ArticleCategoryRepository implements IArticleCategoryRepository {
    @Resource
    private IArticleCategoryDao articleCategoryDao;

    @Override
    public void saveArticleCategory(Long articleId, Long categoryId) {
        ArticleCategory build = ArticleCategory.builder().articleId(articleId).categoryId(categoryId).build();
        articleCategoryDao.insert(build);
    }

    @Override
    public void deleteArticleCategory(Long articleId) {
        articleCategoryDao.deleteByArticleId(articleId);
    }
}
