package cn.xu.domain.post.service;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.policy.PostHotScoreStrategy;
import cn.xu.domain.post.model.policy.PostHotScoreStrategyFactory;
import cn.xu.infrastructure.cache.RedisKeyManager;
import cn.xu.infrastructure.cache.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 帖子热度管理领域服务
 * 负责帖子热度计算和排名管理，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostHotScoreDomainService {

    private final RedisService redisService;

    /**
     * 更新帖子热度分数
     * @param postId 帖子ID
     * @param post 帖子实体
     */
    public void updateHotScore(Long postId, PostEntity post) {
        if (postId == null || post == null) {
            log.warn("更新帖子热度失败：参数为空 - postId: {}", postId);
            return;
        }

        try {
            // 使用策略模式计算热度分数
            PostHotScoreStrategy strategy = PostHotScoreStrategyFactory.getStrategy();
            double hotScore = strategy.calculate(
                    post.getLikeCount() != null ? post.getLikeCount() : 0,
                    post.getCommentCount() != null ? post.getCommentCount() : 0,
                    post.getViewCount() != null ? post.getViewCount() : 0,
                    post.getCreateTime()
            );

            // 更新到缓存
            redisService.zSetAdd(RedisKeyManager.postHotRankKey(), postId.toString(), hotScore);

            log.info("帖子热度更新成功 - postId: {}, hotScore: {}", postId, hotScore);
        } catch (Exception e) {
            log.error("更新帖子热度失败 - postId: {}", postId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 批量更新帖子热度
     * @param posts 帖子列表
     */
    public void batchUpdateHotScore(List<PostEntity> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }

        try {
            PostHotScoreStrategy strategy = PostHotScoreStrategyFactory.getStrategy();

            for (PostEntity post : posts) {
                if (post.getId() != null) {
                    double hotScore = strategy.calculate(
                            post.getLikeCount() != null ? post.getLikeCount() : 0,
                            post.getCommentCount() != null ? post.getCommentCount() : 0,
                            post.getViewCount() != null ? post.getViewCount() : 0,
                            post.getCreateTime()
                    );

                    redisService.zSetAdd(RedisKeyManager.postHotRankKey(), post.getId().toString(), hotScore);
                }
            }

            log.info("批量更新帖子热度成功 - 数量: {}", posts.size());
        } catch (Exception e) {
            log.error("批量更新帖子热度失败", e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 获取热度排行榜
     * @param start 开始位置
     * @param end 结束位置
     * @return 帖子ID列表
     */
    public List<String> getHotRankRange(int start, int end) {
        try {
            return redisService.zSetReverseRange(RedisKeyManager.postHotRankKey(), start, end);
        } catch (Exception e) {
            log.error("获取热度排行榜失败 - start: {}, end: {}", start, end, e);
            throw new BusinessException("获取热度排行榜失败：" + e.getMessage());
        }
    }

    /**
     * 获取前N篇热门帖子ID
     * @param topN 获取数量
     * @return 帖子ID列表
     */
    public List<String> getTopNHotPosts(int topN) {
        if (topN <= 0) {
            return java.util.Collections.emptyList();
        }

        return getHotRankRange(0, topN - 1);
    }

    /**
     * 从热度排行榜中移除帖子
     * @param postId 帖子ID
     */
    public void removeFromHotRank(Long postId) {
        if (postId == null) {
            return;
        }

        try {
            redisService.zSetRemove(RedisKeyManager.postHotRankKey(), postId.toString());
            log.info("从热度排行榜移除帖子成功 - postId: {}", postId);
        } catch (Exception e) {
            log.error("从热度排行榜移除帖子失败 - postId: {}", postId, e);
            // 不抛异常，避免影响主流程
        }
    }

    /**
     * 获取帖子的热度分数
     * @param postId 帖子ID
     * @return 热度分数，不存在返回null
     */
    public Double getHotScore(Long postId) {
        if (postId == null) {
            return null;
        }

        try {
            return redisService.zSetScore(RedisKeyManager.postHotRankKey(), postId.toString());
        } catch (Exception e) {
            log.error("获取帖子热度分数失败 - postId: {}", postId, e);
            return null;
        }
    }

    /**
     * 清理热度排行榜（保留前N名）
     * @param keepTopN 保留的前N名数量
     */
    public void cleanupHotRank(long keepTopN) {
        try {
            long totalCount = redisService.zSetSize(RedisKeyManager.postHotRankKey());
            if (totalCount > keepTopN) {
                // 移除排名靠后的数据
                redisService.zSetRemoveRange(RedisKeyManager.postHotRankKey(), 0, totalCount - keepTopN - 1);
                log.info("清理热度排行榜成功 - 保留前{}名，清理{}条数据", keepTopN, totalCount - keepTopN);
            }
        } catch (Exception e) {
            log.error("清理热度排行榜失败 - keepTopN: {}", keepTopN, e);
        }
    }
}