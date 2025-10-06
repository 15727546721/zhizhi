package cn.xu.infrastructure.cache;

import cn.xu.infrastructure.constant.LogConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作工具类
 * 提供连接池管理、健康检查和各种Redis数据结构操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查Redis连接是否正常
     * 
     * @return true表示连接正常，false表示连接异常
     */
    public boolean isRedisHealthy() {
        try {
            String healthCheckKey = "health_check";
            String healthCheckValue = "ok";
            redisTemplate.opsForValue().set(healthCheckKey, healthCheckValue, 1, TimeUnit.SECONDS);
            String value = (String) redisTemplate.opsForValue().get(healthCheckKey);
            return healthCheckValue.equals(value);
        } catch (Exception e) {
            log.error("Redis健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取Redis连接池信息
     * 
     * @return 连接池信息字符串
     */
    public String getConnectionPoolInfo() {
        try {
            // 获取Redis连接
            try (org.springframework.data.redis.connection.RedisConnection connection = 
                 redisTemplate.getConnectionFactory().getConnection()) {
                // 获取连接信息
                java.util.Properties info = connection.info();
                
                // 提取关键连接池信息
                String connectedClients = info.getProperty("connected_clients", "0");
                String usedMemory = info.getProperty("used_memory_human", "0");
                String totalKeys = "0";
                
                // 尝试获取键空间信息
                try {
                    String db0Info = info.getProperty("db0", "keys=0,expires=0,avg_ttl=0");
                    totalKeys = db0Info.split(",")[0].split("=")[1];
                } catch (Exception e) {
                    log.warn("获取键数量信息失败: {}", e.getMessage());
                }
                
                return String.format("连接数: %s, 内存使用: %s, 键总数: %s", 
                                   connectedClients, usedMemory, totalKeys);
            }
        } catch (Exception e) {
            log.error("获取Redis连接池信息失败: {}", e.getMessage());
            return "无法获取连接池信息: " + e.getMessage();
        }
    }

    /**
     * 设置过期时间
     */
    public void expire(String key, long time) {
        try {
            long startTime = System.currentTimeMillis();
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();
            log.info(LogConstants.REDIS_OPERATION_SUCCESS, "设置过期时间", key, endTime - startTime);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "设置过期时间", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取过期时间
     */
    public long getExpire(String key) {
        try {
            long startTime = System.currentTimeMillis();
            long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();
            if (expire == -2) {
                log.info(LogConstants.REDIS_KEY_NOT_EXIST, key);
            } else if (expire == -1) {
                log.info(LogConstants.REDIS_OPERATION_SUCCESS, "获取过期时间(永不过期)", key, endTime - startTime);
            } else {
                log.info(LogConstants.REDIS_OPERATION_SUCCESS, "获取过期时间", key, endTime - startTime);
            }
            return expire;
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取过期时间", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Boolean result = redisTemplate.hasKey(key);
            long endTime = System.currentTimeMillis();
            if (Boolean.TRUE.equals(result)) {
                log.info(LogConstants.CACHE_HIT, key, endTime - startTime);
            } else {
                log.info(LogConstants.CACHE_MISS, key);
            }
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "判断key是否存在", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 删除缓存
     */
    public void del(String... keys) {
        if (keys != null && keys.length > 0) {
            try {
                long startTime = System.currentTimeMillis();
                if (keys.length == 1) {
                    redisTemplate.delete(keys[0]);
                    long endTime = System.currentTimeMillis();
                    log.info(LogConstants.CACHE_EVICT, keys[0], "主动删除");
                } else {
                    redisTemplate.delete(Arrays.asList(keys));
                    long endTime = System.currentTimeMillis();
                    log.info(LogConstants.REDIS_OPERATION_SUCCESS, "批量删除", String.join(",", keys), endTime - startTime);
                }
            } catch (Exception e) {
                log.error(LogConstants.REDIS_OPERATION_FAILED, "删除缓存", String.join(",", keys), e.getMessage());
                throw e;
            }
        }
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     */
    public Object get(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Object value = redisTemplate.opsForValue().get(key);
            long endTime = System.currentTimeMillis();
            if (value != null) {
                log.info(LogConstants.CACHE_HIT, key, endTime - startTime);
            } else {
                log.info(LogConstants.CACHE_MISS, key);
            }
            return value;
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "获取缓存", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 普通缓存放入
     */
    public void set(String key, Object value) {
        try {
            long startTime = System.currentTimeMillis();
            redisTemplate.opsForValue().set(key, value);
            long endTime = System.currentTimeMillis();
            log.info(LogConstants.CACHE_UPDATE, key, -1);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "设置缓存", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 普通缓存放入并设置时间
     */
    public void set(String key, Object value, long time) {
        try {
            long startTime = System.currentTimeMillis();
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            long endTime = System.currentTimeMillis();
            log.info(LogConstants.CACHE_UPDATE, key, time);
        } catch (Exception e) {
            log.error(LogConstants.REDIS_OPERATION_FAILED, "设置缓存及过期时间", key, e.getMessage());
            throw e;
        }
    }

    /**
     * 递增
     */
    public long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     */
    public long decr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ================================Hash=================================

    /**
     * HashGet
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     */
    public void hmset(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    // ============================Set=============================

    /**
     * 根据key获取Set中的所有值
     */
    public Set<Object> sGet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 将数据放入set缓存
     */
    @SuppressWarnings("unchecked")
    public void sSet(String key, Object... values) {
        if (values.length == 1) {
            redisTemplate.opsForSet().add(key, values[0]);
        } else {
            redisTemplate.opsForSet().add(key, values);
        }
    }

    /**
     * 移除值为value的
     */
    @SuppressWarnings("unchecked")
    public void setRemove(String key, Object... values) {
        redisTemplate.opsForSet().remove(key, values);
    }

    // ===============================ZSet=================================

    /**
     * 添加元素,有序集合是按分数由小到大排列
     */
    public void zAdd(String key, Object value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取集合的元素, 从小到大排序
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取集合元素, 并且把score值也获取
     */
    public Set<ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * 根据Score值查询集合元素
     */
    public Set<Object> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * ZSet添加元素
     */
    public void zSetAdd(String key, String value, double score) {
        try {
            long startTime = System.currentTimeMillis();
            redisTemplate.opsForZSet().add(key, value, score);
            long endTime = System.currentTimeMillis();
            log.debug("ZSet添加元素成功 - key: {}, value: {}, score: {}, 耗时: {}ms", 
                    key, value, score, endTime - startTime);
        } catch (Exception e) {
            log.error("ZSet添加元素失败 - key: {}, value: {}, score: {}", key, value, score, e);
            throw e;
        }
    }

    /**
     * ZSet获取指定范围元素（按分数递减排序）
     */
    public List<String> zSetReverseRange(String key, long start, long end) {
        try {
            long startTime = System.currentTimeMillis();
            Set<Object> result = redisTemplate.opsForZSet().reverseRange(key, start, end);
            long endTime = System.currentTimeMillis();
            
            List<String> stringList = result != null ? 
                result.stream().map(Object::toString).collect(java.util.stream.Collectors.toList()) :
                java.util.Collections.emptyList();
                
            log.debug("ZSet获取元素成功 - key: {}, start: {}, end: {}, count: {}, 耗时: {}ms", 
                    key, start, end, stringList.size(), endTime - startTime);
            return stringList;
        } catch (Exception e) {
            log.error("ZSet获取元素失败 - key: {}, start: {}, end: {}", key, start, end, e);
            throw e;
        }
    }

    /**
     * ZSet移除元素
     */
    public void zSetRemove(String key, String value) {
        try {
            long startTime = System.currentTimeMillis();
            redisTemplate.opsForZSet().remove(key, value);
            long endTime = System.currentTimeMillis();
            log.debug("ZSet移除元素成功 - key: {}, value: {}, 耗时: {}ms", 
                    key, value, endTime - startTime);
        } catch (Exception e) {
            log.error("ZSet移除元素失败 - key: {}, value: {}", key, value, e);
            throw e;
        }
    }

    /**
     * ZSet获取元素分数
     */
    public Double zSetScore(String key, String value) {
        try {
            long startTime = System.currentTimeMillis();
            Double score = redisTemplate.opsForZSet().score(key, value);
            long endTime = System.currentTimeMillis();
            log.debug("ZSet获取元素分数成功 - key: {}, value: {}, score: {}, 耗时: {}ms", 
                    key, value, score, endTime - startTime);
            return score;
        } catch (Exception e) {
            log.error("ZSet获取元素分数失败 - key: {}, value: {}", key, value, e);
            throw e;
        }
    }

    /**
     * ZSet获取集合大小
     */
    public Long zSetSize(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Long size = redisTemplate.opsForZSet().size(key);
            long endTime = System.currentTimeMillis();
            log.debug("ZSet获取集合大小成功 - key: {}, size: {}, 耗时: {}ms", 
                    key, size, endTime - startTime);
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.error("ZSet获取集合大小失败 - key: {}", key, e);
            return 0L;
        }
    }

    /**
     * ZSet移除指定范围的元素（按排名）
     */
    public void zSetRemoveRange(String key, long start, long end) {
        try {
            long startTime = System.currentTimeMillis();
            redisTemplate.opsForZSet().removeRange(key, start, end);
            long endTime = System.currentTimeMillis();
            log.debug("ZSet移除范围元素成功 - key: {}, start: {}, end: {}, 耗时: {}ms", 
                    key, start, end, endTime - startTime);
        } catch (Exception e) {
            log.error("ZSet移除范围元素失败 - key: {}, start: {}, end: {}", key, start, end, e);
            throw e;
        }
    }

    // ===============================List=================================

    /**
     * 获取list缓存的内容
     */
    public List<Object> lGet(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 将list放入缓存
     */
    public void lSet(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 将list放入缓存
     */
    public void lSet(String key, List<Object> value) {
        redisTemplate.opsForList().rightPushAll(key, value);
    }

    // ===============================HyperLogLog=================================

    /**
     * 添加到HyperLogLog
     */
    public void pfAdd(String key, String... values) {
        if (values.length == 1) {
            redisTemplate.opsForHyperLogLog().add(key, values[0]);
        } else {
            redisTemplate.opsForHyperLogLog().add(key, (Object[]) values);
        }
    }

    /**
     * 获取HyperLogLog统计数据
     */
    public long pfCount(String key) {
        return redisTemplate.opsForHyperLogLog().size(key);
    }
    
    // ===============================Post View Count=================================
    
    /**
     * 增加帖子浏览量
     * @param postId 帖子ID
     */
    public void incrementViewCount(Long postId) {
        if (postId == null) {
            return;
        }
        
        String key = "post:view:count:" + postId;
        redisTemplate.opsForValue().increment(key, 1);
        
        // 设置过期时间，避免key无限增长
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }
    
    /**
     * 获取帖子浏览量
     * @param postId 帖子ID
     * @return 浏览量
     */
    public Long getViewCount(Long postId) {
        if (postId == null) {
            return 0L;
        }
        
        String key = "post:view:count:" + postId;
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }
} 