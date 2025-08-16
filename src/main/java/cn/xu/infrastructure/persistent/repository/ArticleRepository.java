package cn.xu.infrastructure.persistent.repository;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.ArticleStatus;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.infrastructure.persistent.dao.ArticleMapper;
import cn.xu.infrastructure.persistent.dao.ArticleTagMapper;
import cn.xu.infrastructure.persistent.po.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ArticleRepository implements IArticleRepository {

    @Resource
    private ArticleMapper articleDao;
    @Resource
    private ArticleTagMapper articleTagDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public Long save(ArticleEntity articleEntity) {
        log.info("保存文章 {}: ", articleEntity);
        Article article = Article.builder()
                .title(articleEntity.getTitle())
                .userId(articleEntity.getUserId())
                .description(articleEntity.getDescription())
                .content(articleEntity.getContent())
                .coverUrl(articleEntity.getCoverUrl())
                .status(articleEntity.getStatus().getValue())
                .build();
        //插入后得到的id值会赋给article的id属性
        articleDao.insert(article);
        return article.getId();
    }

    @Override
    public List<ArticlePageVO> queryArticle(ArticleRequest articleRequest) {
        articleRequest.setPageNo(articleRequest.getPageNo() - 1);
        List<ArticlePageVO> articles = articleDao.queryByPage(articleRequest);
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

    public Article findPoById(Long id) {
        return articleDao.findById(id);
    }

    @Override
    public void update(ArticleEntity articleEntity) {
        log.info("更新文章 {}", articleEntity);
        Article article = Article.builder()
                .id(articleEntity.getId())
                .title(articleEntity.getTitle())
                .userId(articleEntity.getUserId())
                .description(articleEntity.getDescription())
                .content(articleEntity.getContent())
                .coverUrl(articleEntity.getCoverUrl())
                .status(articleEntity.getStatus().getValue())
                .build();
        articleDao.update(article);
    }

    @Override
    public List<ArticleEntity> queryArticleByPage(int page, int size) {
        return articleDao.queryArticleByPage(page, size);
    }

    @Override
    public List<ArticleListVO> queryArticleByCategoryId(Long categoryId) {
        return articleDao.queryByCategoryId(categoryId);
    }

    @Override
    public void updateArticleLikeCount(Long articleId, Long likeCount) {

    }

    @Override
    public void batchUpdateArticleLikeCount(Map<Long, Long> likeCounts) {

    }

    @Override
    public List<ArticleEntity> findAllPublishedArticles() {
        List<Article> articles = articleDao.findAllPublishedArticles();
        return articles.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleEntity> findAll() {
        List<Article> articles = articleDao.findAll();
        return articles.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleListVO> queryArticleByUserId(Long userId) {
        return articleDao.queryByUserId(userId);
    }

    @Override
    public void updateArticleStatus(Integer status, Long id) {
        articleDao.updateStatus(status, id);
    }

    @Override
    public List<ArticleListVO> queryDraftArticleListByUserId(Long userId) {
        return articleDao.queryDraftArticleListByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting article by ID: {}", id);
        articleDao.deleteById(id);
    }

    @Override
    public List<ArticleEntity> getArticlePageListByCategoryId(Long categoryId, Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Article> page = articleDao.getArticlePageByCategory(categoryId, offset, pageSize);
        return page.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleEntity> getArticlePageList(Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;

        return articleDao.getArticlePageList(offset, pageSize);
    }

    // 修改 convert 方法的参数类型
    private ArticleEntity convert(Article article) {
        return ArticleEntity.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .content(article.getContent())
                .coverUrl(article.getCoverUrl())
                .userId(article.getUserId())
                .status(ArticleStatus.valueOf(article.getStatus()))
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .collectCount(article.getCollectCount())
                .build();
    }

}
