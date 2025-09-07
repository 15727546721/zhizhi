package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.policy.ArticleHotScoreStrategy;
import cn.xu.domain.article.model.policy.ArticleHotScoreStrategyFactory;
import cn.xu.infrastructure.cache.RedisService;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章热度管理领域服务
 * 负责文章热度计算和排名管理，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleHotScoreDomainService {

    private final RedisService redisService;

    private static final String HOT_RANK_KEY = "article:hot:rank";

    /**
     * 更新文章热度分数
     * @param articleId 文章ID
     * @param article 文章实体
     */
    public void updateHotScore(Long articleId, ArticleEntity article) {
        if (articleId == null || article == null) {
            log.warn("更新文章热度失败：参数为空 - articleId: {}", articleId);
            return;
        }

        try {
            // 使用策略模式计算热度分数
            ArticleHotScoreStrategy strategy = ArticleHotScoreStrategyFactory.getStrategy();
            double hotScore = strategy.calculate(
                    article.getLikeCount() != null ? article.getLikeCount() : 0,
                    article.getCommentCount() != null ? article.getCommentCount() : 0,
                    article.getViewCount() != null ? article.getViewCount() : 0,
                    article.getCreateTime()
            );

            // 更新到缓存
            redisService.zSetAdd(HOT_RANK_KEY, articleId.toString(), hotScore);

            log.info("文章热度更新成功 - articleId: {}, hotScore: {}", articleId, hotScore);
        } catch (Exception e) {
            log.error("更新文章热度失败 - articleId: {}", articleId, e);
            throw new BusinessException("更新文章热度失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新文章热度
     * @param articles 文章列表
     */
    public void batchUpdateHotScore(List<ArticleEntity> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        try {
            ArticleHotScoreStrategy strategy = ArticleHotScoreStrategyFactory.getStrategy();

            for (ArticleEntity article : articles) {
                if (article.getId() != null) {
                    double hotScore = strategy.calculate(
                            article.getLikeCount() != null ? article.getLikeCount() : 0,
                            article.getCommentCount() != null ? article.getCommentCount() : 0,
                            article.getViewCount() != null ? article.getViewCount() : 0,
                            article.getCreateTime()
                    );

                    redisService.zSetAdd(HOT_RANK_KEY, article.getId().toString(), hotScore);
                }
            }

            log.info("批量更新文章热度成功 - 数量: {}", articles.size());
        } catch (Exception e) {
            log.error("批量更新文章热度失败", e);
            throw new BusinessException("批量更新文章热度失败：" + e.getMessage());
        }
    }

    /**
     * 获取热度排行榜
     * @param start 开始位置
     * @param end 结束位置
     * @return 文章ID列表
     */
    public List<String> getHotRankRange(int start, int end) {
        try {
            return redisService.zSetReverseRange(HOT_RANK_KEY, start, end);
        } catch (Exception e) {
            log.error("获取热度排行榜失败 - start: {}, end: {}", start, end, e);
            throw new BusinessException("获取热度排行榜失败：" + e.getMessage());
        }
    }

    /**
     * 获取前N篇热门文章ID
     * @param topN 获取数量
     * @return 文章ID列表
     */
    public List<String> getTopNHotArticles(int topN) {
        if (topN <= 0) {
            return java.util.Collections.emptyList();
        }

        return getHotRankRange(0, topN - 1);
    }

    /**
     * 从热度排行榜中移除文章
     * @param articleId 文章ID
     */
    public void removeFromHotRank(Long articleId) {
        if (articleId == null) {
            return;
        }

        try {
            redisService.zSetRemove(HOT_RANK_KEY, articleId.toString());
            log.info("从热度排行榜移除文章成功 - articleId: {}", articleId);
        } catch (Exception e) {
            log.error("从热度排行榜移除文章失败 - articleId: {}", articleId, e);
            // 不抛异常，避免影响主流程
        }
    }

    /**
     * 获取文章的热度分数
     * @param articleId 文章ID
     * @return 热度分数，不存在返回null
     */
    public Double getHotScore(Long articleId) {
        if (articleId == null) {
            return null;
        }

        try {
            return redisService.zSetScore(HOT_RANK_KEY, articleId.toString());
        } catch (Exception e) {
            log.error("获取文章热度分数失败 - articleId: {}", articleId, e);
            return null;
        }
    }

    /**
     * 清理热度排行榜（保留前N名）
     * @param keepTopN 保留的前N名数量
     */
    public void cleanupHotRank(long keepTopN) {
        try {
            long totalCount = redisService.zSetSize(HOT_RANK_KEY);
            if (totalCount > keepTopN) {
                // 移除排名靠后的数据
                redisService.zSetRemoveRange(HOT_RANK_KEY, 0, totalCount - keepTopN - 1);
                log.info("清理热度排行榜成功 - 保留前{}名，清理{}条数据", keepTopN, totalCount - keepTopN);
            }
        } catch (Exception e) {
            log.error("清理热度排行榜失败 - keepTopN: {}", keepTopN, e);
        }
    }
}