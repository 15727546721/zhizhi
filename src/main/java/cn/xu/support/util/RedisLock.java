package cn.xu.support.util;

import cn.xu.cache.core.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 * <p>兼容层：内部委托给 {@link DistributedLock} 实现</p>
 * 
 * @deprecated 建议直接使用 {@link cn.xu.cache.core.DistributedLock}
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Deprecated
public class RedisLock {

    private final DistributedLock distributedLock;
    
    private static final long DEFAULT_EXPIRE_TIME = 10;
    private static final int DEFAULT_RETRY_TIMES = 10;
    private static final long DEFAULT_WAIT_TIME = 100;

    /**
     * 尝试获取锁
     */
    public boolean tryLock(String key) {
        return distributedLock.tryLock(key, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 尝试获取锁
     */
    public boolean tryLock(String key, long expireTime, TimeUnit timeUnit) {
        return distributedLock.tryLock(key, expireTime, timeUnit);
    }

    /**
     * 释放锁
     */
    public boolean releaseLock(String key) {
        return distributedLock.unlock(key);
    }

    /**
     * 尝试获取锁（带重试）
     */
    public boolean tryLockWithRetry(String key, int maxRetries) {
        return distributedLock.tryLockWithRetry(key, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS, maxRetries, DEFAULT_WAIT_TIME);
    }

    /**
     * 尝试获取锁（带重试）
     */
    public boolean tryLockWithRetry(String key, long expireTime, TimeUnit timeUnit, int maxRetries, long waitTimeMs) {
        return distributedLock.tryLockWithRetry(key, expireTime, timeUnit, maxRetries, waitTimeMs);
    }

    /**
     * 执行带锁的操作
     */
    public <T> T executeWithLock(String key, java.util.function.Supplier<T> action) {
        return distributedLock.executeWithLock(key, action);
    }

    /**
     * 执行带锁的操作
     */
    public <T> T executeWithLock(String key, long expireTime, TimeUnit timeUnit, java.util.function.Supplier<T> action) {
        return distributedLock.executeWithLock(key, expireTime, timeUnit, action);
    }

    /**
     * 执行带锁的操作（无返回值）
     */
    public boolean executeWithLock(String key, Runnable action) {
        return distributedLock.executeWithLock(key, action);
    }

    /**
     * 执行带锁的操作（无返回值）
     */
    public boolean executeWithLock(String key, long expireTime, TimeUnit timeUnit, Runnable action) {
        return distributedLock.executeWithLock(key, expireTime, timeUnit, action);
    }
}
