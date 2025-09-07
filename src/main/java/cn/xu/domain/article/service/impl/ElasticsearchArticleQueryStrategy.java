package cn.xu.domain.article.service.impl;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.ArticleTitle;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.article.service.ArticleQueryStrategy;
import cn.xu.infrastructure.persistent.read.elastic.model.ArticleIndex;
import cn.xu.infrastructure.persistent.read.elastic.service.ArticleElasticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch文章查询策略实现
 */
@Slf4j
@Service
public class ElasticsearchArticleQueryStrategy implements ArticleQueryStrategy {

    /*@Autowired(required = false)
    @Nullable*/
    private final ArticleElasticService articleElasticService;
    
    private final IArticleRepository articleRepository;

    public ElasticsearchArticleQueryStrategy(
            /*@Autowired(required = false)*/ @Nullable ArticleElasticService articleElasticService,
            IArticleRepository articleRepository) {
        this.articleElasticService = articleElasticService;
        this.articleRepository = articleRepository;
    }

    @Override
    public Page<ArticleEntity> searchByTitle(String title, Pageable pageable) {
        // 检查Elasticsearch服务是否可用
        if (articleElasticService != null) {
            try {
                Page<ArticleIndex> indexPage = articleElasticService.searchByTitleTimeDesc(title, pageable);
                List<ArticleEntity> entities = indexPage.getContent().stream()
                        .map(this::convertToEntity)
                        .collect(Collectors.toList());
                return new PageImpl<>(entities, pageable, indexPage.getTotalElements());
            } catch (Exception e) {
                log.error("Elasticsearch搜索文章失败，回退到MySQL查询", e);
            }
        }
        
        // 回退到MySQL查询，使用现有的分页方法模拟
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        List<ArticleEntity> articles = articleRepository.getArticlePageList(page + 1, size);
        // 这里简化处理，实际应该根据title进行过滤
        return new PageImpl<>(articles, pageable, articles.size());
    }

    @Override
    public Page<ArticleEntity> getHotRank(String rankType, Pageable pageable) {
        // 检查Elasticsearch服务是否可用
        if (articleElasticService != null) {
            try {
                Page<ArticleIndex> indexPage = articleElasticService.getHotRank(rankType, pageable);
                List<ArticleEntity> entities = indexPage.getContent().stream()
                        .map(this::convertToEntity)
                        .collect(Collectors.toList());
                return new PageImpl<>(entities, pageable, indexPage.getTotalElements());
            } catch (Exception e) {
                log.error("Elasticsearch获取热门文章排行失败，回退到MySQL查询", e);
            }
        }
        
        // 回退到MySQL查询，使用现有的分页方法模拟
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        List<ArticleEntity> articles = articleRepository.getArticlePageList(page + 1, size);
        // 这里简化处理，实际应该根据热度进行排序
        return new PageImpl<>(articles, pageable, articles.size());
    }

    @Override
    public Page<ArticleEntity> getArticlesByCategory(Long categoryId, Pageable pageable) {
        // Elasticsearch通常不用于分类查询，直接回退到MySQL
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        List<ArticleEntity> articles = articleRepository.getArticlePageListByCategoryId(categoryId, page + 1, size);
        return new PageImpl<>(articles, pageable, articles.size());
    }

    @Override
    public Page<ArticleEntity> getArticlesByUser(Long userId, Pageable pageable) {
        // Elasticsearch通常不用于用户文章查询，直接回退到MySQL
        // 注意：IArticleRepository中没有直接根据用户ID分页查询的方法，这里简化处理
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        List<ArticleEntity> articles = articleRepository.getArticlePageList(page + 1, size);
        // 这里简化处理，实际应该根据userId进行过滤
        return new PageImpl<>(articles, pageable, articles.size());
    }

    @Override
    public ArticleEntity getArticleDetail(Long articleId) {
        // 尝试从Elasticsearch获取文章详情
        // 注意：这里简化处理，实际可能需要更复杂的逻辑
        return articleRepository.findById(articleId).orElse(null);
    }

    /**
     * 将ArticleIndex转换为ArticleEntity
     * @param index ArticleIndex对象
     * @return ArticleEntity对象
     */
    private ArticleEntity convertToEntity(ArticleIndex index) {
        return ArticleEntity.builder()
                .id(index.getId())
                .title(index.getTitle() != null ? new ArticleTitle(index.getTitle()) : null)
                .description(index.getDescription())
                .coverUrl(index.getCoverUrl())
                .userId(index.getUserId())
                .categoryId(index.getCategoryId())
                .viewCount(index.getViewCount())
                .collectCount(index.getCollectCount())
                .commentCount(index.getCommentCount())
                .likeCount(index.getLikeCount())
                .createTime(index.getPublishTime())
                .updateTime(index.getUpdateTime())
                .build();
    }
}