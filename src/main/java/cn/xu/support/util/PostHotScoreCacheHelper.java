package cn.xu.support.util;

import cn.xu.cache.RedisKeyManager;
import cn.xu.cache.core.RedisOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 帖子热度缓存工具类
 * 用于处理帖子点赞、收藏、评论等操作对热度的影响
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostHotScoreCacheHelper {

    private final RedisOperations redisOps;

    /**
     * 增加点赞数
     */
    public void incrementLike(Long postId) {
        increment(postId, "like");
    }

    /**
     * 增加收藏数
     */
    public void incrementCollect(Long postId) {
        increment(postId, "collect");
    }

    /**
     * 增加评论数
     */
    public void incrementComment(Long postId) {
        increment(postId, "comment");
    }

    /**
     * 通用增加计数方法
     */
    private void increment(Long postId, String type) {
        String key = RedisKeyManager.postHotCacheKey(postId);
        redisOps.hIncrement(key, type, 1);
        redisOps.expire(key, 3600);
    }

    /**
     * 清理热度数据
     */
    public void clearHotData(Long postId) {
        redisOps.delete(RedisKeyManager.postHotCacheKey(postId));
    }
}