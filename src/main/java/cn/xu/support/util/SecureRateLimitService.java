package cn.xu.support.util;

import cn.xu.cache.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 安全的频控服务
 * <p>特性：基于Redis的原子性频控，Redis不可用时自动降级到内存频控</p>
 
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecureRateLimitService {

    private final RedisService redisService;
    
    // 内存降级频控（Redis不可用时使用）
    private final ConcurrentHashMap<String, AtomicLong> memoryCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> memoryExpireTimes = new ConcurrentHashMap<>();

    /**
     * 固定窗口频控检查
     * 
     * @param key Redis键
     * @param limit 限制次数
     * @param windowSeconds 窗口时间（秒）
     * @return 检查结果
     */
    public RateLimitResult checkFixedWindow(String key, int limit, int windowSeconds) {
        try {
            // 使用Redis INCR进行原子递增
            long newCount = redisService.incr(key, 1L);
            if (newCount == 1) {
                // 第一次设置过期时间
                redisService.expire(key, windowSeconds);
            }
            
            boolean allowed = newCount <= limit;
            log.debug("[频控] 固定窗口检查 - key: {}, 当前计数: {}, 限制: {}, 允许: {}", 
                     key, newCount, limit, allowed);
            
            return new RateLimitResult(
                allowed,
                (int) newCount,
                limit,
                windowSeconds,
                "固定窗口频控"
            );
            
        } catch (Exception e) {
            log.error("[频控] Redis频控异常，降级到内存频控 - key: {}", key, e);
            return checkMemoryFallback(key, limit, windowSeconds);
        }
    }

    /**
     * 滑动窗口频控检查（简化版，使用固定窗口替代）
     */
    public RateLimitResult checkSlidingWindow(String key, int limit, long windowMillis) {
        return checkFixedWindow(key, limit, (int) (windowMillis / 1000));
    }

    /**
     * 用户多窗口频控检查（简化版）
     */
    public RateLimitResult checkUserMultiWindow(Long userId) {
        // 简化为单窗口检查：1分钟5次
        String key = "pm:rate:user:" + userId;
        return checkFixedWindow(key, 5, 60);
    }

    /**
     * 内存降级频控（Redis不可用时使用）
     * 已优化：使用 synchronized 保证计数器操作的原子性，避免竞态条件
     */
    private synchronized RateLimitResult checkMemoryFallback(String key, int limit, int windowSeconds) {
        long currentTime = System.currentTimeMillis();
        
        // 清理过期的计数器
        cleanupExpiredCounters(currentTime);
        
        // 检查当前计数
        AtomicLong counter = memoryCounters.computeIfAbsent(key, k -> new AtomicLong(0));
        Long expireTime = memoryExpireTimes.get(key);
        
        // 如果计数器已过期，重置
        if (expireTime == null || currentTime > expireTime) {
            counter.set(0);
            memoryExpireTimes.put(key, currentTime + windowSeconds * 1000L);
        }
        
        long currentCount = counter.get();
        if (currentCount >= limit) {
            log.warn("[频控] 内存降级频控拒绝 - key: {}, 当前计数: {}, 限制: {}", key, currentCount, limit);
            return RateLimitResult.deny((int) currentCount, limit, "内存降级频控");
        }
        
        // 增加计数
        long newCount = counter.incrementAndGet();
        log.debug("[频控] 内存降级频控通过 - key: {}, 新计数: {}, 限制: {}", key, newCount, limit);
        
        return RateLimitResult.allow((int) newCount, limit, "内存降级频控");
    }

    /**
     * 清理过期的内存计数器
     */
    private void cleanupExpiredCounters(long currentTime) {
        try {
            memoryExpireTimes.entrySet().removeIf(entry -> {
                if (currentTime > entry.getValue()) {
                    memoryCounters.remove(entry.getKey());
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            log.warn("[频控] 清理过期计数器失败", e);
        }
    }

    /**
     * 频控检查结果
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final int currentCount;
        private final int limit;
        private final int windowSeconds;
        private final String strategy;
        private final String message;

        public RateLimitResult(boolean allowed, int currentCount, int limit, int windowSeconds, String strategy) {
            this.allowed = allowed;
            this.currentCount = currentCount;
            this.limit = limit;
            this.windowSeconds = windowSeconds;
            this.strategy = strategy;
            this.message = allowed ? "频控检查通过" : "频控限制触发";
        }

        public RateLimitResult(boolean allowed, int currentCount, int limit, String message) {
            this.allowed = allowed;
            this.currentCount = currentCount;
            this.limit = limit;
            this.windowSeconds = 0;
            this.strategy = "未知";
            this.message = message;
        }

        public static RateLimitResult allow(int currentCount, int limit, String strategy) {
            return new RateLimitResult(true, currentCount, limit, 0, strategy);
        }

        public static RateLimitResult deny(int currentCount, int limit, String message) {
            return new RateLimitResult(false, currentCount, limit, message);
        }

        // Getters
        public boolean isAllowed() { return allowed; }
        public int getCurrentCount() { return currentCount; }
        public int getLimit() { return limit; }
        public int getWindowSeconds() { return windowSeconds; }
        public String getStrategy() { return strategy; }
        public String getMessage() { return message; }
        
        public String getDetailMessage() {
            return String.format("频控结果：%s，当前计数：%d/%d，策略：%s", 
                message, currentCount, limit, strategy);
        }
    }
}