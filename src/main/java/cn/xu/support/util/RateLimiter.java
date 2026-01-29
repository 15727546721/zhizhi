package cn.xu.support.util;

import cn.xu.cache.core.RedisOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * 基于Redis的限流工具类
 * <p>使用滑动窗口算法实现限流</p>
 */
@Slf4j
public class RateLimiter {
    
    private final RedisOperations redisOps;
    private final String keyPrefix;
    private final int maxRequests;
    private final int windowSeconds;
    
    /**
     * 创建限流器
     * 
     * @param redisOps Redis操作封装
     * @param keyPrefix key前缀
     * @param maxRequests 时间窗口内最大请求数
     * @param windowSeconds 时间窗口（秒）
     */
    public RateLimiter(RedisOperations redisOps, String keyPrefix, 
                       int maxRequests, int windowSeconds) {
        this.redisOps = redisOps;
        this.keyPrefix = keyPrefix;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }
    
    /**
     * 检查是否允许请求
     * 
     * @param identifier 标识符（如IP地址或用户ID）
     * @return true表示允许，false表示超过限制
     */
    public boolean allowRequest(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        
        String key = keyPrefix + ":" + identifier;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (windowSeconds * 1000L);
        
        try {
            String memberId = java.util.UUID.randomUUID().toString();
            String luaScript = 
                "local key = KEYS[1]\n" +
                "local windowStartStr = ARGV[1]\n" +
                "local maxRequestsStr = ARGV[2]\n" +
                "local currentTimeStr = ARGV[3]\n" +
                "local windowSecondsStr = ARGV[4]\n" +
                "local memberId = ARGV[5]\n" +
                "\n" +
                "local windowStart = tonumber(windowStartStr)\n" +
                "local maxRequests = tonumber(maxRequestsStr)\n" +
                "local currentTime = tonumber(currentTimeStr)\n" +
                "local windowSeconds = tonumber(windowSecondsStr)\n" +
                "\n" +
                "if not windowStart or not maxRequests or not currentTime or not windowSeconds then\n" +
                "    return 0\n" +
                "end\n" +
                "\n" +
                "redis.call('ZREMRANGEBYSCORE', key, '-inf', windowStartStr)\n" +
                "\n" +
                "local count = redis.call('ZCARD', key)\n" +
                "count = tonumber(count) or 0\n" +
                "maxRequests = tonumber(maxRequestsStr)\n" +
                "\n" +
                "if count < maxRequests then\n" +
                "    redis.call('ZADD', key, currentTimeStr, memberId)\n" +
                "    redis.call('EXPIRE', key, windowSecondsStr)\n" +
                "    return 1\n" +
                "else\n" +
                "    return 0\n" +
                "end";
            
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(luaScript);
            script.setResultType(Long.class);
            
            Long result = redisOps.execute(script, 
                Collections.singletonList(key),
                String.valueOf(windowStart),
                String.valueOf(maxRequests),
                String.valueOf(currentTime),
                String.valueOf(windowSeconds),
                memberId);
            
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("限流检查失败，拒绝请求以防止滥用: identifier={}", identifier, e);
            // 当 Redis 不可用时，拒绝请求以防止滥用
            return false;
        }
    }
    
    /**
     * 获取剩余请求次数
     */
    public int getRemainingRequests(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return 0;
        }
        
        String key = keyPrefix + ":" + identifier;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (windowSeconds * 1000L);
        
        try {
            redisOps.zRemoveRangeByScore(key, 0, windowStart);
            long count = redisOps.zSize(key);
            int remaining = maxRequests - (int) count;
            return Math.max(0, remaining);
        } catch (Exception e) {
            log.error("获取剩余请求次数失败: identifier={}", identifier, e);
            return maxRequests;
        }
    }
}