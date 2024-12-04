package cn.xu.infrastructure.persistent.repository;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.infrastructure.persistent.dao.IArticleDao;
import cn.xu.infrastructure.persistent.dao.IArticleTagDao;
import cn.xu.infrastructure.persistent.po.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Repository
public class ArticleRepository implements IArticleRepository {

    @Resource
    private IArticleDao articleDao;
    @Resource
    private IArticleTagDao articleTagDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public Long save(ArticleEntity articleEntity) {
        log.info("保存文章 articleEntity: " + articleEntity);
        Article article = Article.builder()
                .id(articleEntity.getId())
                .title(articleEntity.getTitle())
                .authorId(StpUtil.getLoginIdAsLong())
                .description(articleEntity.getDescription())
                .content(articleEntity.getContent())
                .coverUrl(articleEntity.getCoverUrl())
                .commentEnabled(articleEntity.getCommentEnabled())
                .status(articleEntity.getStatus())
                .build();
        return articleDao.insert(article);
    }

    @Override
    public List<ArticleEntity> queryArticle(int page, int size) {
        log.info("query article page: " + page + " size: " + size);
        List<Article> articles = articleDao.queryByPage(page - 1, size);
        log.info("query article result: " + articles);
        List<ArticleEntity> articleEntities = new LinkedList<>();
        for (Article article : articles) {
            ArticleEntity articleEntity = ArticleEntity.builder()
                    .id(article.getId())
                    .title(article.getTitle())
                    .description(article.getDescription())
                    .content(article.getContent())
                    .coverUrl(article.getCoverUrl())
                    .commentEnabled(article.getCommentEnabled())
                    .status(article.getStatus())
                    .build();
            articleEntities.add(articleEntity);
        }
        return articleEntities;
    }

    @Override
    public void deleteByIds(List<Long> articleIds) {
        log.info("Deleting articles by IDs: {}", articleIds);

        // 编程式事务
        transactionTemplate.execute(status -> {
            try {
                // 批量删除文章标签
                articleTagDao.deleteByArticleIds(articleIds);
                // 批量删除文章
                articleDao.deleteByIds(articleIds);
                return 1; // 返回成功
            } catch (Exception e) {
                log.error("Error deleting articles: {}", e.getMessage(), e);
                status.setRollbackOnly(); // 设置事务回滚
                return 0; // 返回失败
            }
        });
    }

    @Override
    public ArticleEntity findById(Long id) {
        return convert(articleDao.findById(id));
    }

    @Override
    public void update(ArticleEntity articleEntity) {
        log.info("更新文章 articleEntity: " + articleEntity);
        Article article = Article.builder()
                .id(articleEntity.getId())
                .title(articleEntity.getTitle())
                .authorId(articleEntity.getAuthorId())
                .description(articleEntity.getDescription())
                .content(articleEntity.getContent())
                .coverUrl(articleEntity.getCoverUrl())
                .commentEnabled(articleEntity.getCommentEnabled())
                .status(articleEntity.getStatus())
                .build();
        articleDao.update(article);
    }

    private ArticleEntity convert(Article article) {
        return ArticleEntity.builder()
               .id(article.getId())
               .title(article.getTitle())
               .description(article.getDescription())
               .content(article.getContent())
               .coverUrl(article.getCoverUrl())
               .commentEnabled(article.getCommentEnabled())
               .status(article.getStatus())
               .build();
    }
}
