package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.repository.IArticleCategoryRepository;
import cn.xu.infrastructure.persistent.dao.IArticleCategoryDao;
import cn.xu.infrastructure.persistent.po.ArticleCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Repository
public class ArticleCategoryRepository implements IArticleCategoryRepository {
    @Resource
    private IArticleCategoryDao articleCategoryDao;

    @Override
    public void saveArticleCategory(Long articleId, Long categoryId) {
        log.info("保存文章分类 articleId:{},categoryId:{}", articleId, categoryId);
        ArticleCategory build = ArticleCategory.builder().articleId(articleId).categoryId(categoryId).build();
        articleCategoryDao.insert(build);
    }

    @Override
    public void deleteArticleCategory(Long articleId) {
        articleCategoryDao.deleteByArticleId(articleId);
    }
}
