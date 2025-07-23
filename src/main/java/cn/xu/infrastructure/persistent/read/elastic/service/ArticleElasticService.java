package cn.xu.infrastructure.persistent.read.elastic.service;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.ArticleHotScorePolicy;
import cn.xu.infrastructure.persistent.po.Article;
import cn.xu.infrastructure.persistent.read.elastic.model.ArticleIndex;
import cn.xu.infrastructure.persistent.read.elastic.repository.ArticleElasticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleElasticService {

    private final ArticleElasticRepository articleElasticRepository;

    public void indexArticle(ArticleEntity article) {
        ArticleIndex index = ArticleIndexConverter.from(article);
        articleElasticRepository.save(index);
    }

    public void updateIndexedArticle(ArticleEntity article) {
        ArticleIndex index = ArticleIndexConverter.from(article);
        articleElasticRepository.save(index);
    }

    public void removeIndexedArticle(Long articleId) {
        articleElasticRepository.deleteById(articleId);
    }

    // 初始化或更新索引
    public void indexArticle(Article article) {
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
    }

    // 获取热度排行（日榜，周榜，月榜）
    public Page<ArticleIndex> getHotRank(String rankType, Pageable pageable) {
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
    }

    // 简单时间降序搜索
    public Page<ArticleIndex> searchByTitleTimeDesc(String keyword, Pageable pageable) {
        return articleElasticRepository.findByTitleContainingOrderByPublishTimeDesc(keyword, pageable);
    }

    // 搜索文章（示例）
    public List<ArticleEntity> searchArticles(String title) {
        Page<ArticleIndex> page = articleElasticRepository.findByTitleContainingOrderByPublishTimeDesc(title, Pageable.unpaged());
        return page.stream()
                .map(this::toArticleEntity)
                .collect(Collectors.toList());
    }

    private ArticleEntity toArticleEntity(ArticleIndex index) {
        return ArticleEntity.builder()
                .id(index.getId())
                .title(index.getTitle())
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

