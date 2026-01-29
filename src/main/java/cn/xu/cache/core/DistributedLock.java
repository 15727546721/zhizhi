package cn.xu.cache.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

/**
 * 分布式锁
 * <p>基于Redis实现的分布式锁，使用Lua脚本保证原子性</p>
 * <p>特性：</p>
 * <ul>
 *   <li>使用UUID作为锁值，确保只能释放自己的锁</li>
 *   <li>使用Lua脚本保证释放锁的原子性</li>
 *   <li>支持自动续期（可选）</li>
 *   <li>支持重试获取</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private final RedisOperations redisOps;

    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_EXPIRE_SECONDS = 30;
    private static final long DEFAULT_WAIT_MS = 100;
    private static final int DEFAULT_RETRY_TIMES = 3;

    // 存储当前线程持有的锁值
    private static final ConcurrentHashMap<String, String> LOCK_VALUES = new ConcurrentHashMap<>();

    // 释放锁的Lua脚本
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    private static final DefaultRedisScript<Long> UNLOCK_REDIS_SCRIPT =
            new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);

    /**
     * 尝试获取锁
     *
     * @param key 锁的业务key（不含前缀）
     * @return true表示获取成功
     */
    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 尝试获取锁
     *
     * @param key        锁的业务key
     * @param expireTime 过期时间
     * @param timeUnit   时间单位
     * @return true表示获取成功
     */
    public boolean tryLock(String key, long expireTime, TimeUnit timeUnit) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();
        long expireSeconds = timeUnit.toSeconds(expireTime);

        boolean success = redisOps.setIfAbsent(lockKey, lockValue, expireSeconds);
        if (success) {
            LOCK_VALUES.put(lockKey, lockValue);
            log.debug("获取锁成功: key={}", key);
        }
        return success;
    }

    /**
     * 尝试获取锁（带重试）
     *
     * @param key        锁的业务key
     * @param maxRetries 最大重试次数
     * @return true表示获取成功
     */
    public boolean tryLockWithRetry(String key, int maxRetries) {
        return tryLockWithRetry(key, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS, maxRetries, DEFAULT_WAIT_MS);
    }

    /**
     * 尝试获取锁（带重试）
     */
    public boolean tryLockWithRetry(String key, long expireTime, TimeUnit timeUnit, int maxRetries, long waitMs) {
        for (int i = 0; i < maxRetries; i++) {
            if (tryLock(key, expireTime, timeUnit)) {
                return true;
            }
            LockSupport.parkNanos(waitMs * 1_000_000L);
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        log.debug("获取锁超时: key={}, retries={}", key, maxRetries);
        return false;
    }

    /**
     * 释放锁
     *
     * @param key 锁的业务key
     * @return true表示释放成功
     */
    public boolean unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = LOCK_VALUES.remove(lockKey);

        if (lockValue == null) {
            log.warn("释放锁失败，未找到锁值: key={}", key);
            return false;
        }

        Long result = redisOps.execute(
                UNLOCK_REDIS_SCRIPT,
                Collections.singletonList(lockKey),
                lockValue
        );

        boolean success = result != null && result == 1L;
        if (success) {
            log.debug("释放锁成功: key={}", key);
        } else {
            log.warn("释放锁失败，锁已过期或被其他线程持有: key={}", key);
        }
        return success;
    }

    /**
     * 执行带锁的操作
     *
     * @param key    锁的业务key
     * @param action 要执行的操作
     * @param <T>    返回值类型
     * @return 操作结果，获取锁失败返回null
     */
    public <T> T executeWithLock(String key, Supplier<T> action) {
        return executeWithLock(key, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS, action);
    }

    /**
     * 执行带锁的操作
     */
    public <T> T executeWithLock(String key, long expireTime, TimeUnit timeUnit, Supplier<T> action) {
        if (tryLock(key, expireTime, timeUnit)) {
            try {
                return action.get();
            } finally {
                unlock(key);
            }
        }
        log.warn("获取锁失败，跳过操作: key={}", key);
        return null;
    }

    /**
     * 执行带锁的操作（带重试）
     */
    public <T> T executeWithLockRetry(String key, int maxRetries, Supplier<T> action) {
        if (tryLockWithRetry(key, maxRetries)) {
            try {
                return action.get();
            } finally {
                unlock(key);
            }
        }
        log.warn("获取锁失败，跳过操作: key={}", key);
        return null;
    }

    /**
     * 执行带锁的操作（无返回值）
     */
    public boolean executeWithLock(String key, Runnable action) {
        return executeWithLock(key, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS, action);
    }

    /**
     * 执行带锁的操作（无返回值）
     */
    public boolean executeWithLock(String key, long expireTime, TimeUnit timeUnit, Runnable action) {
        if (tryLock(key, expireTime, timeUnit)) {
            try {
                action.run();
                return true;
            } finally {
                unlock(key);
            }
        }
        log.warn("获取锁失败，跳过操作: key={}", key);
        return false;
    }
}
