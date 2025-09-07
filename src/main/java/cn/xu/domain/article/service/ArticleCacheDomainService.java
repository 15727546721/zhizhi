package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.infrastructure.cache.ArticleCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章缓存领域服务
 * 专门处理文章相关的缓存逻辑，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleCacheDomainService {

    private final ArticleCacheRepository articleCacheRepository;

    /**
     * 从缓存获取热门文章ID列表
     * @param start 开始位置
     * @param end 结束位置
     * @return 文章ID列表
     */
    public List<Long> getHotArticleIdsFromCache(int start, int end) {
        try {
            List<Long> articleIds = articleCacheRepository.getHotArticleIds(start, end);
            log.debug("从缓存获取热门文章成功 - start: {}, end: {}, count: {}", 
                    start, end, articleIds != null ? articleIds.size() : 0);
            return articleIds;
        } catch (Exception e) {
            log.error("从缓存获取热门文章失败 - start: {}, end: {}", start, end, e);
            return Collections.emptyList();
        }
    }

    /**
     * 缓存热门文章排行
     * @param articles 文章列表
     */
    public void cacheHotArticleRank(List<ArticleEntity> articles) {
        try {
            if (articles == null || articles.isEmpty()) {
                log.info("文章列表为空，缓存空结果");
                articleCacheRepository.cacheEmptyResult();
                return;
            }

            Map<Long, Double> articleScores = articles.stream()
                    .filter(article -> article.getId() != null)
                    .collect(Collectors.toMap(
                            ArticleEntity::getId, 
                            this::calculateHotScore));
            
            articleCacheRepository.cacheHotArticleRank(articleScores);
            log.info("缓存热门文章排行成功 - count: {}", articleScores.size());
        } catch (Exception e) {
            log.error("缓存热门文章排行失败 - count: {}", 
                    articles != null ? articles.size() : 0, e);
            // 缓存失败不影响主流程，只记录错误日志
        }
    }

    /**
     * 更新单个文章的热度缓存
     * @param articleId 文章ID
     * @param hotScore 热度分数
     */
    public void updateHotRank(Long articleId, double hotScore) {
        if (articleId == null) {
            log.warn("更新热度缓存失败：文章ID为空");
            return;
        }
        
        try {
            articleCacheRepository.updateHotRank(articleId, hotScore);
            log.debug("更新文章热度缓存成功 - articleId: {}, hotScore: {}", articleId, hotScore);
        } catch (Exception e) {
            log.error("更新文章热度缓存失败 - articleId: {}, hotScore: {}", articleId, hotScore, e);
            // 缓存失败不影响主流程
        }
    }

    /**
     * 缓存空结果，防止缓存穿透
     */
    public void cacheEmptyResult() {
        articleCacheRepository.cacheEmptyResult();
    }

    /**
     * 检查是否为空结果缓存
     * @return 是否为空结果
     */
    public boolean isEmptyResultCached() {
        try {
            boolean isEmpty = articleCacheRepository.isEmptyResultCached();
            log.debug("检查空结果缓存 - isEmpty: {}", isEmpty);
            return isEmpty;
        } catch (Exception e) {
            log.error("检查空结果缓存失败", e);
            return false;
        }
    }

    /**
     * 按顺序排列文章
     * @param orderedIds 有序的ID列表
     * @param articles 文章列表
     * @return 按ID顺序排列的文章列表
     */
    public List<ArticleEntity> sortArticlesByIds(List<Long> orderedIds, List<ArticleEntity> articles) {
        if (orderedIds.isEmpty() || articles.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, ArticleEntity> articleMap = articles.stream()
                .collect(Collectors.toMap(ArticleEntity::getId, Function.identity()));

        return orderedIds.stream()
                .map(articleMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 清理无效的缓存数据
     * @param allIds 所有ID列表
     * @param validArticles 有效的文章列表
     */
    public void cleanupInvalidCacheData(List<Long> allIds, List<ArticleEntity> validArticles) {
        Set<Long> validIds = validArticles.stream()
                .map(ArticleEntity::getId)
                .collect(Collectors.toSet());
        
        List<Long> invalidIds = allIds.stream()
                .filter(id -> !validIds.contains(id))
                .collect(Collectors.toList());
        
        if (!invalidIds.isEmpty()) {
            articleCacheRepository.cleanupInvalidCacheData(invalidIds);
        }
    }

    /**
     * 获取前N篇热度最高的文章ID
     * @param topN 获取的文章数量
     * @return 热度前N篇文章的ID
     */
    public List<Long> getTopNHotArticleIds(int topN) {
        if (topN <= 0) {
            return Collections.emptyList();
        }
        
        return articleCacheRepository.getHotArticleIds(0, topN - 1);
    }

    /**
     * 计算文章热度分数
     * 这里使用简单的计算逻辑，实际项目中可能需要更复杂的算法
     * @param article 文章实体
     * @return 热度分数
     */
    private double calculateHotScore(ArticleEntity article) {
        if (article == null) {
            return 0.0;
        }
        
        long likeCount = article.getLikeCount() != null ? article.getLikeCount() : 0;
        long commentCount = article.getCommentCount() != null ? article.getCommentCount() : 0;
        long viewCount = article.getViewCount() != null ? article.getViewCount() : 0;
        
        // 简单的热度计算公式：点赞数*3 + 评论数*2 + 浏览数*0.1
        return likeCount * 3.0 + commentCount * 2.0 + viewCount * 0.1;
    }

    /**
     * 批量更新文章热度
     * @param articles 文章列表
     */
    public void batchUpdateHotRank(List<ArticleEntity> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }
        
        Map<Long, Double> articleScores = articles.stream()
                .filter(article -> article.getId() != null)
                .collect(Collectors.toMap(
                        ArticleEntity::getId, 
                        this::calculateHotScore));
        
        articleCacheRepository.batchUpdateHotRank(articleScores);
    }

    /**
     * 删除文章的热度缓存
     * @param articleId 文章ID
     */
    public void removeFromHotRank(Long articleId) {
        if (articleId == null) {
            log.warn("删除热度缓存失败：文章ID为空");
            return;
        }
        
        try {
            articleCacheRepository.removeFromHotRank(articleId);
            log.debug("删除文章热度缓存成功 - articleId: {}", articleId);
        } catch (Exception e) {
            log.error("删除文章热度缓存失败 - articleId: {}", articleId, e);
            // 缓存失败不影响主流程
        }
    }
}