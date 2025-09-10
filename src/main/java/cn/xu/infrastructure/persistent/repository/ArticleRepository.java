package cn.xu.infrastructure.persistent.repository;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.infrastructure.persistent.converter.ArticleConverter;
import cn.xu.infrastructure.persistent.dao.ArticleMapper;
import cn.xu.infrastructure.persistent.dao.ArticleTagMapper;
import cn.xu.infrastructure.persistent.po.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文章仓储实现
 * 遵循DDD原则，处理文章实体级别的操作
 * 注意：对于聚合根级别的操作，应使用ArticleAggregateRepositoryImpl
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ArticleRepository implements IArticleRepository {

    private final ArticleMapper articleDao;
    private final ArticleTagMapper articleTagDao;
    private final TransactionTemplate transactionTemplate;
    private final ArticleConverter articleConverter;

    @Override
    public Long save(ArticleEntity articleEntity) {
        Article article = articleConverter.toDataObject(articleEntity);
        articleDao.insert(article);
        return article.getId();
    }

    @Override
    public List<ArticlePageVO> queryArticle(ArticleRequest articleRequest) {
        articleRequest.setPageNo(articleRequest.getPageNo() - 1);
        return articleDao.queryByPage(articleRequest);
    }

    @Override
    public void deleteByIds(List<Long> articleIds) {
        transactionTemplate.execute(status -> {
            try {
                articleTagDao.deleteByArticleIds(articleIds);
                articleDao.deleteByIds(articleIds);
                return 1;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("删除文章失败", e);
            }
        });
    }

    @Override
    public Optional<ArticleEntity> findById(Long id) {
        Article article = articleDao.findById(id);
        return article != null ? Optional.of(articleConverter.toDomainEntity(article)) : Optional.empty();
    }

    public Article findPoById(Long id) {
        return articleDao.findById(id);
    }

    @Override
    public void update(ArticleEntity articleEntity) {
        Article article = articleConverter.toDataObject(articleEntity);
        articleDao.update(article);
    }

    @Override
    public List<ArticleEntity> queryArticleByPage(int page, int size) {
        List<Article> articles = articleDao.queryArticleByPage(page, size);
        return articleConverter.toDomainEntities(articles);
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
        return articleConverter.toDomainEntities(articles);
    }

    @Override
    public List<ArticleEntity> findAll() {
        List<Article> articles = articleDao.findAll();
        return articleConverter.toDomainEntities(articles);
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
        articleDao.deleteById(id);
    }

    @Override
    public List<ArticleEntity> getArticlePageListByCategoryId(Long categoryId, Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Article> articles = articleDao.getArticlePageByCategory(categoryId, offset, pageSize);
        return articleConverter.toDomainEntities(articles);
    }

    /**
     * 分页查询文章列表
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 文章列表
     */
    public List<ArticleEntity> getArticlePageList(Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Article> articles = articleDao.getArticlePageList(offset, pageSize);
        return articleConverter.toDomainEntities(articles);
    }

    /**
     * 分页查询文章列表（支持排序）
     */
    public List<ArticleEntity> getArticlePageListWithSort(Integer pageNo, Integer pageSize, String sortBy) {
        int offset = (pageNo - 1) * pageSize;
        List<Article> articles = articleDao.getArticlePageListWithSort(offset, pageSize, sortBy);
        return articleConverter.toDomainEntities(articles);
    }
}
