package cn.xu.support.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * 基于Redis的限流工具类
 * <p>使用滑动窗口算法实现限流</p>
 
 */
@Slf4j
public class RateLimiter {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final String keyPrefix;
    private final int maxRequests;
    private final int windowSeconds;
    
    /**
     * 创建限流器
     * 
     * @param redisTemplate Redis模板
     * @param keyPrefix key前缀
     * @param maxRequests 时间窗口内最大请求数
     * @param windowSeconds 时间窗口（秒）
     */
    public RateLimiter(RedisTemplate<String, Object> redisTemplate, String keyPrefix, 
                       int maxRequests, int windowSeconds) {
        this.redisTemplate = redisTemplate;
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
            // 使用Lua脚本实现原子操作
            // 使用UUID作为member，避免同一毫秒内的请求覆盖
            String memberId = java.util.UUID.randomUUID().toString();
            String luaScript = 
                "local key = KEYS[1]\n" +
                "local windowStartStr = ARGV[1]\n" +
                "local maxRequestsStr = ARGV[2]\n" +
                "local currentTimeStr = ARGV[3]\n" +
                "local windowSecondsStr = ARGV[4]\n" +
                "local memberId = ARGV[5]\n" +
                "\n" +
                "-- 转换为数字（用于比较）\n" +
                "local windowStart = tonumber(windowStartStr)\n" +
                "local maxRequests = tonumber(maxRequestsStr)\n" +
                "local currentTime = tonumber(currentTimeStr)\n" +
                "local windowSeconds = tonumber(windowSecondsStr)\n" +
                "\n" +
                "-- 检查参数是否有误\n" +
                "if not windowStart or not maxRequests or not currentTime or not windowSeconds then\n" +
                "    return 0\n" +
                "end\n" +
                "\n" +
                "-- 移除窗口外的记录（使用字符串参数，Redis会自动转换）\n" +
                "-- 移除score小于等于windowStart的记录（窗口外的记录）\n" +
                "redis.call('ZREMRANGEBYSCORE', key, '-inf', windowStartStr)\n" +
                "\n" +
                "-- 获取当前窗口内的请求数\n" +
                "local count = redis.call('ZCARD', key)\n" +
                "\n" +
                "-- 转换为数字进行比较\n" +
                "count = tonumber(count) or 0\n" +
                "maxRequests = tonumber(maxRequestsStr)\n" +
                "\n" +
                "if count < maxRequests then\n" +
                "    -- 允许请求，记录当前时间戳和唯一标识\n" +
                "    -- ZADD的score使用字符串参数，Redis会自动转换为数字\n" +
                "    redis.call('ZADD', key, currentTimeStr, memberId)\n" +
                "    -- EXPIRE使用字符串参数，Redis会自动转换为数字\n" +
                "    redis.call('EXPIRE', key, windowSecondsStr)\n" +
                "    return 1\n" +
                "else\n" +
                "    return 0\n" +
                "end";
            
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(luaScript);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(script, 
                Collections.singletonList(key),
                String.valueOf(windowStart),
                String.valueOf(maxRequests),
                String.valueOf(currentTime),
                String.valueOf(windowSeconds),
                memberId);
            
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("限流检查失败: identifier={}", identifier, e);
            // 限流失败时，为了不影响业务，默认允许请求
            return true;
        }
    }
    
    /**
     * 获取剩余请求次数
     * 
     * @param identifier 标识符
     * @return 剩余请求次数
     */
    public int getRemainingRequests(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return 0;
        }
        
        String key = keyPrefix + ":" + identifier;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (windowSeconds * 1000L);
        
        try {
            // 移除窗口外的记录
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
            
            // 获取当前窗口内的请求数
            Long count = redisTemplate.opsForZSet().zCard(key);
            int remaining = maxRequests - (count != null ? count.intValue() : 0);
            return Math.max(0, remaining);
        } catch (Exception e) {
            log.error("获取剩余请求次数失败: identifier={}", identifier, e);
            return maxRequests;
        }
    }
}