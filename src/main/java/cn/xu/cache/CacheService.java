package cn.xu.cache;

import cn.xu.cache.core.DistributedLock;
import cn.xu.cache.core.RedisOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 通用缓存服务
 * <p>
 * 提供带降级处理的缓存操作，解决以下问题：
 * <ul>
 *   <li>缓存穿透：空值缓存</li>
 *   <li>缓存击穿：分布式锁</li>
 *   <li>缓存雪崩：随机过期时间</li>
 *   <li>降级处理：Redis 不可用时直接查库</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisOperations redisOps;
    private final DistributedLock distributedLock;

    // 空值缓存标记
    private static final String NULL_VALUE = "NULL";
    // 空值缓存时间（秒）
    private static final long NULL_CACHE_TTL = 60;
    // 默认缓存时间（秒）
    private static final long DEFAULT_TTL = 300;
    // 随机过期时间范围（秒）
    private static final int RANDOM_TTL_RANGE = 60;
    // 锁等待重试次数
    private static final int LOCK_RETRY_COUNT = 3;
    // 锁等待间隔（毫秒）
    private static final long LOCK_RETRY_INTERVAL = 100;

    // ==================== 单值缓存 ====================

    /**
     * 获取缓存，不存在则从数据源加载
     *
     * @param key      缓存键
     * @param loader   数据加载器
     * @param ttl      过期时间（秒）
     * @param <T>      返回类型
     * @return 缓存值或数据源值
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Supplier<T> loader, long ttl) {
        try {
            // 1. 尝试从缓存获取
            Object cached = redisOps.get(key);
            if (cached != null) {
                if (NULL_VALUE.equals(cached)) {
                    log.debug("缓存命中空值: key={}", key);
                    return null;
                }
                log.debug("缓存命中: key={}", key);
                return (T) cached;
            }

            // 2. 缓存未命中，从数据源加载
            T value = loader.get();

            // 3. 写入缓存（包括空值）
            if (value == null) {
                // 缓存空值，防止缓存穿透
                redisOps.set(key, NULL_VALUE, NULL_CACHE_TTL);
                log.debug("缓存空值: key={}", key);
            } else {
                // 添加随机过期时间，防止缓存雪崩
                long actualTtl = ttl + ThreadLocalRandom.current().nextInt(RANDOM_TTL_RANGE);
                redisOps.set(key, value, actualTtl);
                log.debug("写入缓存: key={}, ttl={}s", key, actualTtl);
            }

            return value;
        } catch (Exception e) {
            // Redis 不可用时降级到直接查库
            log.warn("缓存操作失败，降级到数据源: key={}, error={}", key, e.getMessage());
            return loader.get();
        }
    }

    /**
     * 获取缓存（使用默认过期时间）
     */
    public <T> T getOrLoad(String key, Supplier<T> loader) {
        return getOrLoad(key, loader, DEFAULT_TTL);
    }

    /**
     * 获取缓存，带分布式锁防止缓存击穿
     * <p>
     * 适用于热点数据，防止缓存失效时大量请求同时查询数据库
     * </p>
     *
     * @param key      缓存键
     * @param loader   数据加载器
     * @param ttl      过期时间（秒）
     * @param <T>      返回类型
     * @return 缓存值或数据源值
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoadWithLock(String key, Supplier<T> loader, long ttl) {
        try {
            // 1. 尝试从缓存获取
            Object cached = redisOps.get(key);
            if (cached != null) {
                if (NULL_VALUE.equals(cached)) {
                    return null;
                }
                return (T) cached;
            }

            // 2. 缓存未命中，尝试获取分布式锁
            String lockKey = "cache:" + key;

            for (int i = 0; i < LOCK_RETRY_COUNT; i++) {
                if (distributedLock.tryLock(lockKey)) {
                    try {
                        // 双重检查：获取锁后再次检查缓存
                        cached = redisOps.get(key);
                        if (cached != null) {
                            if (NULL_VALUE.equals(cached)) {
                                return null;
                            }
                            return (T) cached;
                        }

                        // 从数据源加载
                        T value = loader.get();

                        // 写入缓存
                        if (value == null) {
                            redisOps.set(key, NULL_VALUE, NULL_CACHE_TTL);
                        } else {
                            long actualTtl = ttl + ThreadLocalRandom.current().nextInt(RANDOM_TTL_RANGE);
                            redisOps.set(key, value, actualTtl);
                        }

                        return value;
                    } finally {
                        distributedLock.unlock(lockKey);
                    }
                }

                // 未获取到锁，等待后重试
                try {
                    Thread.sleep(LOCK_RETRY_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // 重试前再次检查缓存（可能其他线程已加载完成）
                cached = redisOps.get(key);
                if (cached != null) {
                    if (NULL_VALUE.equals(cached)) {
                        return null;
                    }
                    return (T) cached;
                }
            }

            // 获取锁失败，降级到直接查库
            log.warn("获取分布式锁失败，降级到数据源: key={}", key);
            return loader.get();

        } catch (Exception e) {
            log.warn("缓存操作失败，降级到数据源: key={}, error={}", key, e.getMessage());
            return loader.get();
        }
    }

    /**
     * 获取缓存，带分布式锁（使用默认过期时间）
     */
    public <T> T getOrLoadWithLock(String key, Supplier<T> loader) {
        return getOrLoadWithLock(key, loader, DEFAULT_TTL);
    }

    // ==================== 批量缓存 ====================

    /**
     * 批量获取缓存，不存在的从数据源加载
     *
     * @param keyPrefix  缓存键前缀
     * @param ids        ID 列表
     * @param loader     批量数据加载器
     * @param idExtractor ID 提取器
     * @param ttl        过期时间（秒）
     * @param <ID>       ID 类型
     * @param <T>        返回类型
     * @return Map<ID, T>
     */
    @SuppressWarnings("unchecked")
    public <ID, T> Map<ID, T> batchGetOrLoad(
            String keyPrefix,
            Collection<ID> ids,
            Function<List<ID>, List<T>> loader,
            Function<T, ID> idExtractor,
            long ttl) {

        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ID> idList = new ArrayList<>(ids);
        Map<ID, T> result = new HashMap<>(idList.size());

        try {
            // 1. 构建缓存键
            List<String> keys = idList.stream()
                    .map(id -> keyPrefix + id)
                    .collect(Collectors.toList());

            // 2. 批量获取缓存
            List<Object> cachedValues = redisOps.multiGet(keys);

            // 3. 分离命中和未命中的 ID
            List<ID> missedIds = new ArrayList<>();
            if (cachedValues != null) {
                for (int i = 0; i < idList.size(); i++) {
                    Object cached = cachedValues.get(i);
                    ID id = idList.get(i);
                    if (cached == null) {
                        missedIds.add(id);
                    } else if (!NULL_VALUE.equals(cached)) {
                        result.put(id, (T) cached);
                    }
                    // NULL_VALUE 表示空值缓存，不加入结果
                }
            } else {
                missedIds.addAll(idList);
            }

            log.debug("批量缓存查询: total={}, hit={}, miss={}",
                    idList.size(), result.size(), missedIds.size());

            // 4. 加载未命中的数据
            if (!missedIds.isEmpty()) {
                List<T> loadedValues = loader.apply(missedIds);
                Map<ID, T> loadedMap = loadedValues.stream()
                        .collect(Collectors.toMap(idExtractor, v -> v, (a, b) -> a));

                // 5. 写入缓存
                Map<String, Object> toCache = new HashMap<>();
                for (ID id : missedIds) {
                    T value = loadedMap.get(id);
                    String key = keyPrefix + id;
                    if (value != null) {
                        result.put(id, value);
                        toCache.put(key, value);
                    } else {
                        // 缓存空值
                        toCache.put(key, NULL_VALUE);
                    }
                }

                if (!toCache.isEmpty()) {
                    // 批量写入缓存
                    redisOps.multiSet(toCache);
                    // 设置过期时间
                    long actualTtl = ttl + ThreadLocalRandom.current().nextInt(RANDOM_TTL_RANGE);
                    for (String key : toCache.keySet()) {
                        redisOps.expire(key, actualTtl);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            // Redis 不可用时降级到直接查库
            log.warn("批量缓存操作失败，降级到数据源: error={}", e.getMessage());
            List<T> loadedValues = loader.apply(idList);
            return loadedValues.stream()
                    .collect(Collectors.toMap(idExtractor, v -> v, (a, b) -> a));
        }
    }

    /**
     * 批量获取缓存（使用默认过期时间）
     */
    public <ID, T> Map<ID, T> batchGetOrLoad(
            String keyPrefix,
            Collection<ID> ids,
            Function<List<ID>, List<T>> loader,
            Function<T, ID> idExtractor) {
        return batchGetOrLoad(keyPrefix, ids, loader, idExtractor, DEFAULT_TTL);
    }

    // ==================== 缓存失效 ====================

    /**
     * 删除缓存
     */
    public void evict(String key) {
        redisOps.delete(key);
        log.debug("删除缓存: key={}", key);
    }

    /**
     * 批量删除缓存
     */
    public void batchEvict(String keyPrefix, Collection<?> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<String> keys = ids.stream()
                .map(id -> keyPrefix + id)
                .collect(Collectors.toList());
        redisOps.delete(keys);
        log.debug("批量删除缓存: count={}", keys.size());
    }

    // ==================== 列表缓存 ====================

    /**
     * 获取列表缓存
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getListOrLoad(String key, Supplier<List<T>> loader, long ttl) {
        try {
            Object cached = redisOps.get(key);
            if (cached != null) {
                if (NULL_VALUE.equals(cached)) {
                    return Collections.emptyList();
                }
                return (List<T>) cached;
            }

            List<T> value = loader.get();
            if (value == null || value.isEmpty()) {
                redisOps.set(key, NULL_VALUE, NULL_CACHE_TTL);
            } else {
                long actualTtl = ttl + ThreadLocalRandom.current().nextInt(RANDOM_TTL_RANGE);
                redisOps.set(key, value, actualTtl);
            }
            return value != null ? value : Collections.emptyList();
        } catch (Exception e) {
            log.warn("列表缓存操作失败，降级到数据源: key={}, error={}", key, e.getMessage());
            List<T> value = loader.get();
            return value != null ? value : Collections.emptyList();
        }
    }
}
