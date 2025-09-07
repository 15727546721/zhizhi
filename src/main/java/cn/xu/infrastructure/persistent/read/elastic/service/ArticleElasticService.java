package cn.xu.infrastructure.persistent.read.elastic.service;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.ArticleHotScorePolicy;
import cn.xu.domain.article.model.valobj.ArticleTitle;
import cn.xu.infrastructure.persistent.po.Article;
import cn.xu.infrastructure.persistent.read.elastic.model.ArticleIndex;
import cn.xu.infrastructure.persistent.read.elastic.repository.ArticleElasticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ArticleElasticService {

    private final ArticleElasticRepository articleElasticRepository;

    @PostConstruct
    public void init() {
        // 初始化检查
        try {
            // 尝试执行一个简单的操作来验证连接
            articleElasticRepository.count();
        } catch (Exception e) {
            // 如果连接失败，记录日志但不抛出异常
        }
    }

    public void indexArticle(ArticleEntity article) {
        try {
            ArticleIndex index = ArticleIndexConverter.from(article);
            articleElasticRepository.save(index);
        } catch (Exception e) {
            // 忽略Elasticsearch操作失败，不影响主流程
        }
    }

    public void updateIndexedArticle(ArticleEntity article) {
        try {
            ArticleIndex index = ArticleIndexConverter.from(article);
            articleElasticRepository.save(index);
        } catch (Exception e) {
            // 忽略Elasticsearch操作失败，不影响主流程
        }
    }

    public void removeIndexedArticle(Long articleId) {
        try {
            articleElasticRepository.deleteById(articleId);
        } catch (Exception e) {
            // 忽略Elasticsearch操作失败，不影响主流程
        }
    }

    // 初始化或更新索引
    public void indexArticle(Article article) {
        try {
            ArticleIndex index = new ArticleIndex();
            index.setId(article.getId());
            index.setTitle(article.getTitle());
            index.setDescription(article.getDescription());
            index.setCoverUrl(article.getCoverUrl());
            index.setUserId(article.getUserId());
            index.setCategoryId(article.getCategoryId());
            index.setViewCount(article.getViewCount());
            index.setCollectCount(article.getCollectCount());
            index.setCommentCount(article.getCommentCount());
            index.setLikeCount(article.getLikeCount());
            index.setPublishTime(article.getPublishTime());
            index.setUpdateTime(article.getUpdateTime());

            double hotScore = ArticleHotScorePolicy.calculate(
                    article.getLikeCount(), article.getCollectCount(), article.getCommentCount(), article.getPublishTime());
            index.setHotScore(hotScore);

            articleElasticRepository.save(index);
        } catch (Exception e) {
            // 忽略Elasticsearch操作失败，不影响主流程
        }
    }

    // 获取热度排行（日榜，周榜，月榜）
    public Page<ArticleIndex> getHotRank(String rankType, Pageable pageable) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start;
            switch (rankType) {
                case "day":
                    start = now.minusDays(1);
                    break;
                case "week":
                    start = now.minusWeeks(1);
                    break;
                case "month":
                    start = now.minusMonths(1);
                    break;
                default:
                    start = now.minusDays(1);
            }
            return articleElasticRepository.findByPublishTimeBetweenOrderByHotScoreDesc(start, now, pageable);
        } catch (Exception e) {
            // 如果Elasticsearch不可用，抛出异常让调用方处理
            throw new RuntimeException("Elasticsearch服务不可用", e);
        }
    }

    // 简单时间降序搜索
    public Page<ArticleIndex> searchByTitleTimeDesc(String keyword, Pageable pageable) {
        try {
            return articleElasticRepository.findByTitleContainingOrderByPublishTimeDesc(keyword, pageable);
        } catch (Exception e) {
            // 如果Elasticsearch不可用，抛出异常让调用方处理
            throw new RuntimeException("Elasticsearch服务不可用", e);
        }
    }

    // 搜索文章（示例）
    public List<ArticleEntity> searchArticles(String title) {
        try {
            Page<ArticleIndex> page = articleElasticRepository.findByTitleContainingOrderByPublishTimeDesc(title, Pageable.unpaged());
            return page.stream()
                    .map(this::toArticleEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 如果Elasticsearch不可用，抛出异常让调用方处理
            throw new RuntimeException("Elasticsearch服务不可用", e);
        }
    }

    private ArticleEntity toArticleEntity(ArticleIndex index) {
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