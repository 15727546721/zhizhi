package cn.xu.infrastructure.cache;

import cn.xu.domain.cache.ICacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务实现
 * 基于RedisTemplate实现缓存操作
 */
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements ICacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public Long getCount(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            }
            return value != null ? Long.parseLong(value.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public Map<String, Long> batchGetCount(String... keys) {
        Map<String, Long> result = new HashMap<>();
        try {
            for (String key : keys) {
                Long value = getCount(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
        } catch (Exception e) {
            // 忽略异常，返回已获取的结果
        }
        return result;
    }
    
    @Override
    public void setCount(String key, Long count) {
        redisTemplate.opsForValue().set(key, count);
    }
    
    @Override
    public Long incrementCount(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }
    
    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
    
    @Override
    public void batchDelete(String... keys) {
        for (String key : keys) {
            redisTemplate.delete(key);
        }
    }
    
    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 设置带过期时间的缓存
     * @param key 键
     * @param value 值
     * @param timeout 过期时间（秒）
     */
    public void setWithTimeout(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }
}