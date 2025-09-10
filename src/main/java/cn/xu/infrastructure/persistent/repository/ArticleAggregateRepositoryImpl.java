package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.model.aggregate.ArticleAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.repository.IArticleAggregateRepository;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.infrastructure.persistent.converter.ArticleConverter;
import cn.xu.infrastructure.persistent.dao.ArticleMapper;
import cn.xu.infrastructure.persistent.dao.ArticleTagMapper;
import cn.xu.infrastructure.persistent.po.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文章聚合根仓储实现
 * 遵循DDD原则，管理文章聚合根的持久化操作
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ArticleAggregateRepositoryImpl implements IArticleAggregateRepository {
    
    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleConverter articleConverter;
    private final IArticleTagRepository articleTagRepository;

    @Override
    @Transactional
    public Long save(ArticleAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }

        // 保存文章实体
        ArticleEntity articleEntity = aggregate.getArticleEntity();
        Article article = articleConverter.toDataObject(articleEntity);
        articleMapper.insert(article);
        
        // 更新聚合根ID
        Long articleId = article.getId();
        aggregate.setId(articleId);
        
        // 保存标签关联
        if (aggregate.getTagIds() != null && !aggregate.getTagIds().isEmpty()) {
            articleTagRepository.saveArticleTags(articleId, aggregate.getTagIds());
        }
        
        log.info("保存文章聚合根成功, ID: {}", articleId);
        return articleId;
    }

    @Override
    @Transactional
    public void update(ArticleAggregate aggregate) {
        if (aggregate == null || aggregate.getId() == null) {
            return;
        }

        // 更新文章实体
        ArticleEntity articleEntity = aggregate.getArticleEntity();
        Article article = articleConverter.toDataObject(articleEntity);
        articleMapper.update(article);
        
        // 更新标签关联
        Long articleId = aggregate.getId();
        articleTagMapper.deleteByArticleId(articleId);
        if (aggregate.getTagIds() != null && !aggregate.getTagIds().isEmpty()) {
            articleTagRepository.saveArticleTags(articleId, aggregate.getTagIds());
        }
        
        log.info("更新文章聚合根成功, ID: {}", articleId);
    }

    @Override
    public Optional<ArticleAggregate> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        Article article = articleMapper.findById(id);
        if (article == null) {
            return Optional.empty();
        }

        ArticleEntity articleEntity = articleConverter.toDomainEntity(article);
        List<Long> tagIds = articleTagRepository.findTagIdsByArticleId(id);
        
        ArticleAggregate aggregate = ArticleAggregate.builder()
                .id(id)
                .articleEntity(articleEntity)
                .tagIds(tagIds)
                .build();
                
        return Optional.of(aggregate);
    }

    @Override
    public List<ArticleAggregate> findByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<Article> articles = articleMapper.findByUserId(userId);
        return convertToAggregates(articles);
    }

    @Override
    public List<ArticleAggregate> findPublishedByCategoryId(Long categoryId, Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Article> articles = articleMapper.getArticlePageByCategory(categoryId, offset, pageSize);
        return convertToAggregates(articles);
    }

    @Override
    public List<ArticleAggregate> findPublishedArticles(Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Article> articles = articleMapper.getPublishedArticlePageList(offset, pageSize);
        return convertToAggregates(articles);
    }

    @Override
    public List<ArticleAggregate> findAllPublished() {
        List<Article> articles = articleMapper.findAllPublishedArticles();
        return convertToAggregates(articles);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }

        // 删除标签关联
        articleTagMapper.deleteByArticleId(id);
        
        // 删除文章
        articleMapper.deleteById(id);
        
        log.info("删除文章聚合根成功, ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 删除标签关联
        articleTagMapper.deleteByArticleIds(ids);
        
        // 删除文章
        articleMapper.deleteByIds(ids);
        
        log.info("批量删除文章聚合根成功, IDs: {}", ids);
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return articleMapper.existsById(id);
    }

    @Override
    public Long countPublishedByUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        return articleMapper.countPublishedByUserId(userId);
    }

    /**
     * 转换为聚合根列表
     */
    private List<ArticleAggregate> convertToAggregates(List<Article> articles) {
        return articles.stream()
                .map(article -> {
                    ArticleEntity entity = articleConverter.toDomainEntity(article);
                    List<Long> tagIds = articleTagRepository.findTagIdsByArticleId(article.getId());
                    
                    return ArticleAggregate.builder()
                            .id(article.getId())
                            .articleEntity(entity)
                            .tagIds(tagIds)
                            .build();
                })
                .collect(Collectors.toList());
    }
}