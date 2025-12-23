package cn.xu.support.util;

import cn.xu.cache.RedisKeyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 帖子热度缓存工具类
 * 用于处理帖子点赞、收藏、评论等操作对热度的影响
 * 
 * 
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostHotScoreCacheHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 增加点赞数
     * @param postId 帖子ID
     */
    public void incrementLike(Long postId) {
        increment(postId, "like");
    }

    /**
     * 增加收藏数
     * @param postId 帖子ID
     */
    public void incrementCollect(Long postId) {
        increment(postId, "collect");
    }

    /**
     * 增加评论数
     * @param postId 帖子ID
     */
    public void incrementComment(Long postId) {
        increment(postId, "comment");
    }

    /**
     * 通用增加计数方法
     * @param postId 帖子ID
     * @param type 类型 (like, collect, comment)
     */
    private void increment(Long postId, String type) {
        String key = RedisKeyManager.postHotCacheKey(postId);
        redisTemplate.opsForHash().increment(key, type, 1);
        // 设置过期时间 1 小时
        redisTemplate.expire(key, 3600, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * 清理热度数据
     * @param postId 帖子ID
     */
    public void clearHotData(Long postId) {
        redisTemplate.delete(RedisKeyManager.postHotCacheKey(postId));
    }
}