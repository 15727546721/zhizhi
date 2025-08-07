package cn.xu.infrastructure.common.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 热度统计工具类
 */
@Component
public class ArticleHotScoreCacheHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    public ArticleHotScoreCacheHelper(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void incrementLike(Long articleId) {
        increment(articleId, "like");
    }

    public void incrementCollect(Long articleId) {
        increment(articleId, "collect");
    }

    public void incrementComment(Long articleId) {
        increment(articleId, "comment");
    }

    private void increment(Long articleId, String type) {
        String key = "article:hot:" + articleId;
        redisTemplate.opsForHash().increment(key, type, 1);
        redisTemplate.expire(key, Duration.ofDays(2)); // 可调节热度时间窗口
    }

    public void clearHotData(Long articleId) {
        redisTemplate.delete("article:hot:" + articleId);
    }
}
