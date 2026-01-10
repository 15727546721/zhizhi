package cn.xu.cache;

import cn.xu.common.constants.LogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作服务类
 * <p>提供连接检查、数据存取、过期时间设置等常用的Redis操作</p>
 * 
 * @deprecated 建议使用 {@link cn.xu.cache.core.RedisOperations} 替代
 */
@Component
@Deprecated
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查Redis连接是否正常
     *
     * @return true 表示连接正常，false 表示连接失败
     */
    public boolean isRedisHealthy() {
        try {
            String healthCheckKey = "health_check";
            String healthCheckValue = "ok";
            redisTemplate.opsForValue().set(healthCheckKey, healthCheckValue, 1, TimeUnit.SECONDS);
            String value = (String) redisTemplate.opsForValue().get(healthCheckKey);
            return healthCheckValue.equals(value);
        } catch (Exception e) {
            log.error("Redis连接健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取Redis连接池信息
     *
     * @return Redis连接池信息
     */
    public String getConnectionPoolInfo() {
        try {
            // 获取Redis连接
            try (org.springframework.data.redis.connection.RedisConnection connection =
                         redisTemplate.getConnectionFactory().getConnection()) {
                // 获取连接信息
                java.util.Properties info = connection.info();

                // 获取连接池信息
                String connectedClients = info.getProperty("connected_clients", "0");
                String usedMemory = info.getProperty("used_memory_human", "0");
                String totalKeys = "0";

                // 尝试获取数据库0的信息
                try {
                    String db0Info = info.getProperty("db0", "keys=0,expires=0,avg_ttl=0");
                    totalKeys = db0Info.split(",")[0].split("=")[1];
                } catch (Exception e) {
                    log.warn("获取数据库信息失败: {}", e.getMessage());
                }

                return String.format("连接数: %s, 使用内存: %s, 总键数: %s",
                        connectedClients, usedMemory, totalKeys);
            }
        } catch (Exception e) {
            log.error("获取Redis连接池信息失败: {}", e.getMessage());
            return "获取Redis连接池信息失败: " + e.getMessage();
        }
    }

    /**
     * 设置指定键的过期时间
     */
    public void expire(String key, long time) {
        try {
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
            log.debug("设置过期时间: key={}, time={}秒", key, time);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "设置过期时间失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取指定键的剩余过期时间
     */
    public long getExpire(String key) {
        try {
            long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            log.debug("获取过期时间: key={}, expire={}秒", key, expire);
            return expire;
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取过期时间失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 检查指定键是否存在
     */
    public boolean hasKey(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            log.debug("hasKey: key={}, result={}", key, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "检查key是否存在失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 删除指定的键
     */
    public void del(String... keys) {
        if (keys != null && keys.length > 0) {
            try {
                if (keys.length == 1) {
                    redisTemplate.delete(keys[0]);
                    log.debug("删除缓存: key={}", keys[0]);
                } else {
                    redisTemplate.delete(Arrays.asList(keys));
                    log.debug("批量删除缓存: count={}", keys.length);
                }
            } catch (Exception e) {
                log.error(LogConstants.REDIS_OPERATION_FAILED, "删除缓存失败", String.join(",", keys), e.getMessage());
                throw e;
            }
        }
    }

    // ============================String=============================

    /**
     * 获取指定键的值
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("get: key={}, hit={}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取缓存失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 设置指定键的值
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("set: key={}", key);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "设置缓存失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 设置指定键的值，并设置过期时间
     */
    public void set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            log.debug(LogConstants.CACHE_UPDATE, key, time);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "设置缓存失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 增加指定键的值
     */
    public long incr(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "增加计数失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 减少指定键的值
     */
    public long decr(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, -delta);
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "减少计数失败", key, e.getMessage());
            throw e;
        }
    }

    // ================================Hash=================================

    /**
     * 获取Hash类型缓存中的值
     */
    public Object hget(String key, String item) {
        try {
            return redisTemplate.opsForHash().get(key, item);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取Hash值失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取整个Hash的数据
     */
    public Map<Object, Object> hmget(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取Hash数据失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 设置Hash类型缓存的数据
     */
    public void hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "设置Hash数据失败", key, e.getMessage());
            throw e;
        }
    }

    // ============================Set=============================

    /**
     * 获取Set类型缓存的数据
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取Set数据失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 设置Set类型缓存的数据
     */
    @SafeVarargs
    public final <T> void sSet(String key, T... values) {
        try {
            redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "设置Set数据失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 删除Set中的元素
     */
    @SafeVarargs
    public final <T> void setRemove(String key, T... values) {
        try {
            redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "删除Set元素失败", key, e.getMessage());
            throw e;
        }
    }

    // ===============================ZSet=================================

    /**
     * 添加元素到ZSet
     */
    public void zAdd(String key, Object value, double score) {
        try {
            redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "添加ZSet元素失败", key, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 添加元素到ZSet（别名方法）
     */
    public void zSetAdd(String key, String value, double score) {
        try {
            redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "添加ZSet元素失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取ZSet中的指定范围元素
     */
    public Set<Object> zRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().range(key, start, end);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取ZSet范围元素失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取ZSet中指定范围元素及其score
     */
    public Set<ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取ZSet范围元素失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 根据score获取ZSet中的元素
     */
    public Set<Object> zRangeByScore(String key, double min, double max) {
        try {
            return redisTemplate.opsForZSet().rangeByScore(key, min, max);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "按分数获取ZSet元素失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取ZSet的元素分数
     */
    public Double zSetScore(String key, String value) {
        try {
            return redisTemplate.opsForZSet().score(key, value);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取ZSet元素分数失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取ZSet的元素个数
     */
    public Long zSetSize(String key) {
        try {
            return redisTemplate.opsForZSet().size(key);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取ZSet大小失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 删除ZSet中的元素
     */
    public void zSetRemove(String key, String value) {
        try {
            redisTemplate.opsForZSet().remove(key, value);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "删除ZSet元素失败", key, e.getMessage());
            throw e;
        }
    }

    // ===============================List=================================

    /**
     * 获取List类型缓存的指定范围数据
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取List数据失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 向List添加元素
     */
    public void lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "添加List元素失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 向List添加多个元素
     */
    public void lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "批量添加List元素失败", key, e.getMessage());
            throw e;
        }
    }

    // ===============================HyperLogLog=================================

    /**
     * 向HyperLogLog添加元素
     */
    public void pfAdd(String key, String... values) {
        try {
            redisTemplate.opsForHyperLogLog().add(key, (Object[]) values);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "添加HyperLogLog元素失败", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取HyperLogLog的基数
     */
    public long pfCount(String key) {
        try {
            Long count = redisTemplate.opsForHyperLogLog().size(key);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取HyperLogLog基数失败", key, e.getMessage());
            throw e;
        }
    }

    // ===============================Post View Count=================================

    /**
     * 增加帖子阅读计数
     */
    public void incrementViewCount(Long postId) {
        if (postId == null) {
            return;
        }

        try {
            String key = RedisKeyManager.postViewCountKey(postId);
            redisTemplate.opsForValue().increment(key, 1);
            // 设置过期时间为24小时
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("增加帖子阅读计数失败: postId={}, error={}", postId, e.getMessage());
            // 阅读计数失败不抛异常，降级处理
        }
    }

    /**
     * 获取帖子阅读计数
     */
    public Long getViewCount(Long postId) {
        if (postId == null) {
            return 0L;
        }

        try {
            String key = RedisKeyManager.postViewCountKey(postId);
            Object count = redisTemplate.opsForValue().get(key);
            return count != null ? Long.valueOf(count.toString()) : 0L;
        } catch (Exception e) {
            log.warn("获取帖子阅读计数失败: postId={}, error={}", postId, e.getMessage());
            return 0L;
        }
    }
}
