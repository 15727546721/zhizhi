package cn.xu.cache.core;

import cn.xu.common.constants.LogConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis底层操作封装
 * <p>提供所有Redis数据结构的基础操作，统一异常处理和日志</p>
 * <p>所有上层服务应通过此类访问Redis，而不是直接使用RedisTemplate</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisOperations {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 通用操作 ====================

    /**
     * 检查Redis连接是否正常
     */
    public boolean isHealthy() {
        try {
            String key = "health:check";
            redisTemplate.opsForValue().set(key, "ok", 1, TimeUnit.SECONDS);
            return "ok".equals(redisTemplate.opsForValue().get(key));
        } catch (Exception e) {
            log.error("Redis健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long seconds) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, seconds, TimeUnit.SECONDS));
        } catch (Exception e) {
            log.error("设置过期时间失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 获取剩余过期时间（秒）
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("获取过期时间失败: key={}, error={}", key, e.getMessage());
            return -2;
        }
    }

    /**
     * 检查key是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查key存在失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 删除key
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("删除key失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 批量删除key
     */
    public long delete(Collection<String> keys) {
        try {
            Long count = redisTemplate.delete(keys);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("批量删除key失败: count={}, error={}", keys.size(), e.getMessage());
            return 0;
        }
    }

    // ==================== String操作 ====================

    /**
     * 获取值
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取值失败: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 设置值
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("设置值失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 设置值并设置过期时间
     */
    public boolean set(String key, Object value, long seconds) {
        try {
            if (seconds > 0) {
                redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("设置值失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 如果不存在则设置（原子操作）
     */
    public boolean setIfAbsent(String key, Object value, long seconds) {
        try {
            return Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(key, value, seconds, TimeUnit.SECONDS)
            );
        } catch (Exception e) {
            log.error("setIfAbsent失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 递增
     */
    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("递增失败: key={}, delta={}, error={}", key, delta, e.getMessage());
            return 0;
        }
    }

    /**
     * 批量获取
     */
    public List<Object> multiGet(Collection<String> keys) {
        try {
            return redisTemplate.opsForValue().multiGet(keys);
        } catch (Exception e) {
            log.error("批量获取失败: count={}, error={}", keys.size(), e.getMessage());
            return null;
        }
    }

    /**
     * 批量设置
     */
    public boolean multiSet(Map<String, Object> map) {
        try {
            redisTemplate.opsForValue().multiSet(map);
            return true;
        } catch (Exception e) {
            log.error("批量设置失败: count={}, error={}", map.size(), e.getMessage());
            return false;
        }
    }

    // ==================== Hash操作 ====================

    /**
     * 获取Hash字段值
     */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("获取Hash字段失败: key={}, field={}, error={}", key, field, e.getMessage());
            return null;
        }
    }

    /**
     * 获取整个Hash
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("获取Hash失败: key={}, error={}", key, e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 设置Hash字段
     */
    public boolean hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            return true;
        } catch (Exception e) {
            log.error("设置Hash字段失败: key={}, field={}, error={}", key, field, e.getMessage());
            return false;
        }
    }

    /**
     * 批量设置Hash
     */
    public boolean hSetAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("批量设置Hash失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Hash字段递增
     */
    public long hIncrement(String key, String field, long delta) {
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.error("Hash递增失败: key={}, field={}, error={}", key, field, e.getMessage());
            return 0;
        }
    }

    // ==================== Set操作 ====================

    /**
     * 获取Set所有成员
     */
    public Set<Object> sMembers(String key) {
        try {
            Set<Object> members = redisTemplate.opsForSet().members(key);
            return members != null ? members : Collections.emptySet();
        } catch (Exception e) {
            log.error("获取Set成员失败: key={}, error={}", key, e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 添加Set成员
     */
    public long sAdd(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("添加Set成员失败: key={}, error={}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * 移除Set成员
     */
    public long sRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("移除Set成员失败: key={}, error={}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * 检查是否是Set成员
     */
    public boolean sIsMember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("检查Set成员失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 获取Set大小
     */
    public long sSize(String key) {
        try {
            Long size = redisTemplate.opsForSet().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("获取Set大小失败: key={}, error={}", key, e.getMessage());
            return 0;
        }
    }

    // ==================== ZSet操作 ====================

    /**
     * 添加ZSet成员
     */
    public boolean zAdd(String key, Object value, double score) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
        } catch (Exception e) {
            log.error("添加ZSet成员失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 获取ZSet指定范围（按分数从低到高）
     */
    public Set<Object> zRange(String key, long start, long end) {
        try {
            Set<Object> result = redisTemplate.opsForZSet().range(key, start, end);
            return result != null ? result : Collections.emptySet();
        } catch (Exception e) {
            log.error("获取ZSet范围失败: key={}, error={}", key, e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 获取ZSet指定范围（按分数从高到低）
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        try {
            Set<Object> result = redisTemplate.opsForZSet().reverseRange(key, start, end);
            return result != null ? result : Collections.emptySet();
        } catch (Exception e) {
            log.error("获取ZSet范围失败: key={}, error={}", key, e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 获取ZSet指定范围及分数
     */
    public Set<ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        try {
            Set<ZSetOperations.TypedTuple<Object>> result = redisTemplate.opsForZSet().rangeWithScores(key, start, end);
            return result != null ? result : Collections.emptySet();
        } catch (Exception e) {
            log.error("获取ZSet范围失败: key={}, error={}", key, e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 获取ZSet成员分数
     */
    public Double zScore(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().score(key, value);
        } catch (Exception e) {
            log.error("获取ZSet分数失败: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 移除ZSet成员
     */
    public long zRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForZSet().remove(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("移除ZSet成员失败: key={}, error={}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * 获取ZSet大小
     */
    public long zSize(String key) {
        try {
            Long size = redisTemplate.opsForZSet().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("获取ZSet大小失败: key={}, error={}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * ZSet分数递增
     */
    public double zIncrementScore(String key, Object value, double delta) {
        try {
            Double result = redisTemplate.opsForZSet().incrementScore(key, value, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("ZSet分数递增失败: key={}, error={}", key, e.getMessage());
            return 0;
        }
    }

    // ==================== List操作 ====================

    /**
     * 获取List指定范围
     */
    public List<Object> lRange(String key, long start, long end) {
        try {
            List<Object> result = redisTemplate.opsForList().range(key, start, end);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.error("获取List范围失败: key={}, error={}", key, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 右侧添加元素
     */
    public long rPush(String key, Object value) {
        try {
            Long count = redisTemplate.opsForList().rightPush(key, value);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("List右侧添加失败: key={}, error={}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * 右侧批量添加元素
     */
    public long rPushAll(String key, Collection<Object> values) {
        try {
            Long count = redisTemplate.opsForList().rightPushAll(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("List右侧批量添加失败: key={}, error={}", key, e.getMessage());
            return 0;
        }
    }

    // ==================== HyperLogLog操作 ====================

    /**
     * 添加HyperLogLog元素
     */
    public long pfAdd(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForHyperLogLog().add(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("添加HyperLogLog失败: key={}, error={}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * 获取HyperLogLog基数
     */
    public long pfCount(String key) {
        try {
            Long count = redisTemplate.opsForHyperLogLog().size(key);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("获取HyperLogLog基数失败: key={}, error={}", key, e.getMessage());
            return 0;
        }
    }

    // ==================== Lua脚本执行 ====================

    /**
     * 执行Lua脚本
     */
    public <T> T execute(DefaultRedisScript<T> script, List<String> keys, Object... args) {
        try {
            return redisTemplate.execute(script, keys, args);
        } catch (Exception e) {
            log.error("执行Lua脚本失败: error={}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取底层RedisTemplate（仅供特殊场景使用）
     */
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }
}
