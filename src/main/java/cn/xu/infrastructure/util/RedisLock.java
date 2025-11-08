package cn.xu.infrastructure.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 * 使用SETNX实现简单的分布式锁
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLock {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_EXPIRE_TIME = 10; // 默认过期时间10秒
    private static final long DEFAULT_WAIT_TIME = 100; // 默认等待时间100ms
    private static final int DEFAULT_RETRY_TIMES = 10; // 默认重试次数10次

    /**
     * 尝试获取锁
     * 
     * @param key 锁的key
     * @return true表示获取成功，false表示获取失败
     */
    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 尝试获取锁
     * 
     * @param key 锁的key
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @return true表示获取成功，false表示获取失败
     */
    public boolean tryLock(String key, long expireTime, TimeUnit timeUnit) {
        String lockKey = LOCK_PREFIX + key;
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", expireTime, timeUnit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("获取锁失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 释放锁
     * 
     * @param key 锁的key
     */
    public void releaseLock(String key) {
        String lockKey = LOCK_PREFIX + key;
        try {
            redisTemplate.delete(lockKey);
        } catch (Exception e) {
            log.error("释放锁失败: key={}", key, e);
        }
    }

    /**
     * 尝试获取锁（带重试）
     * 
     * @param key 锁的key
     * @param maxRetries 最大重试次数
     * @return true表示获取成功，false表示获取失败
     */
    public boolean tryLockWithRetry(String key, int maxRetries) {
        return tryLockWithRetry(key, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS, maxRetries, DEFAULT_WAIT_TIME);
    }

    /**
     * 尝试获取锁（带重试）
     * 
     * @param key 锁的key
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @param maxRetries 最大重试次数
     * @param waitTimeMs 等待时间（毫秒）
     * @return true表示获取成功，false表示获取失败
     */
    public boolean tryLockWithRetry(String key, long expireTime, TimeUnit timeUnit, int maxRetries, long waitTimeMs) {
        for (int i = 0; i < maxRetries; i++) {
            if (tryLock(key, expireTime, timeUnit)) {
                return true;
            }
            try {
                Thread.sleep(waitTimeMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待锁被中断: key={}", key);
                return false;
            }
        }
        return false;
    }

    /**
     * 执行带锁的操作
     * 
     * @param key 锁的key
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String key, java.util.function.Supplier<T> action) {
        return executeWithLock(key, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS, action);
    }

    /**
     * 执行带锁的操作
     * 
     * @param key 锁的key
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String key, long expireTime, TimeUnit timeUnit, java.util.function.Supplier<T> action) {
        if (tryLock(key, expireTime, timeUnit)) {
            try {
                return action.get();
            } finally {
                releaseLock(key);
            }
        } else {
            log.warn("获取锁失败，跳过操作: key={}", key);
            return null;
        }
    }

    /**
     * 执行带锁的操作（无返回值）
     * 
     * @param key 锁的key
     * @param action 要执行的操作
     */
    public void executeWithLock(String key, Runnable action) {
        executeWithLock(key, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS, action);
    }

    /**
     * 执行带锁的操作（无返回值）
     * 
     * @param key 锁的key
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @param action 要执行的操作
     */
    public void executeWithLock(String key, long expireTime, TimeUnit timeUnit, Runnable action) {
        if (tryLock(key, expireTime, timeUnit)) {
            try {
                action.run();
            } finally {
                releaseLock(key);
            }
        } else {
            log.warn("获取锁失败，跳过操作: key={}", key);
        }
    }
}

