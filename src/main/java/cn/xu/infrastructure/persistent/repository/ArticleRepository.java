package cn.xu.infrastructure.persistent.repository;

import cn.xu.api.dto.article.ArticlePageResponse;
import cn.xu.api.dto.article.ArticleRequest;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.ArticleRecommendOrNew;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.infrastructure.persistent.dao.IArticleDao;
import cn.xu.infrastructure.persistent.dao.IArticleTagDao;
import cn.xu.infrastructure.persistent.po.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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
        log.info("保存文章 {}: ", articleEntity);
        Article article = Article.builder()
                .title(articleEntity.getTitle())
                .userId(articleEntity.getUserId())
                .introduction(articleEntity.getIntroduction())
                .content(articleEntity.getContent())
                .coverUrl(articleEntity.getCoverUrl())
                .build();
        articleDao.insert(article); //插入后得到的id值会赋给article的id属性
        return article.getId();
    }

    @Override
    public List<ArticlePageResponse> queryArticle(ArticleRequest articleRequest) {
        articleRequest.setPage(articleRequest.getPage() - 1);
        List<ArticlePageResponse> articles = articleDao.queryByPage(articleRequest);
        log.info("查询文章结果: {}", articles);

        return articles;
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
                log.error("删除文章失败: {}", e.getMessage(), e);
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
        log.info("更新文章 {}", articleEntity);
        Article article = Article.builder()
                .id(articleEntity.getId())
                .title(articleEntity.getTitle())
                .userId(articleEntity.getUserId())
                .introduction(articleEntity.getIntroduction())
                .content(articleEntity.getContent())
                .coverUrl(articleEntity.getCoverUrl())
                .build();
        articleDao.update(article);
    }

    @Override
    public List<ArticleRecommendOrNew> queryArticleByPage() {
        List<ArticleRecommendOrNew> articles = articleDao.queryArticleByPage(1 - 1, 10);
        return articles;
    }

    private ArticleEntity convert(Article article) {
        return ArticleEntity.builder()
                .id(article.getId())
                .title(article.getTitle())
                .introduction(article.getIntroduction())
                .content(article.getContent())
                .coverUrl(article.getCoverUrl())
                .build();
    }

    private List<ArticleEntity> convert(List<Article> articles) {
        return articles.stream()
                .map(article -> ArticleEntity.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .introduction(article.getIntroduction())
                        .content(article.getContent())
                        .coverUrl(article.getCoverUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
