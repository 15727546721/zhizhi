package cn.xu.service.statistics;

import cn.xu.cache.core.RedisOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * 统计数据缓存服务
 * <p>为后台统计接口提供缓存支持，减少数据库压力</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsCacheService {

    private final RedisOperations redisOps;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "stats:";
    private static final long DEFAULT_TTL = 300; // 5分钟缓存

    /**
     * 获取缓存数据，如果不存在则执行supplier并缓存结果
     */
    public <T> T getOrCompute(String key, Class<T> type, Supplier<T> supplier) {
        return getOrCompute(key, type, supplier, DEFAULT_TTL);
    }

    /**
     * 获取缓存数据，如果不存在则执行supplier并缓存结果
     */
    public <T> T getOrCompute(String key, Class<T> type, Supplier<T> supplier, long ttlSeconds) {
        String cacheKey = CACHE_PREFIX + key;
        
        try {
            // 尝试从缓存获取
            Object cached = redisOps.get(cacheKey);
            if (cached != null) {
                if (type.isInstance(cached)) {
                    return type.cast(cached);
                }
                // 如果是JSON字符串，尝试反序列化
                if (cached instanceof String) {
                    return objectMapper.readValue((String) cached, type);
                }
            }
        } catch (Exception e) {
            log.warn("读取统计缓存失败: key={}, error={}", key, e.getMessage());
        }
        
        // 缓存未命中，执行查询
        T result = supplier.get();
        
        // 缓存结果
        if (result != null) {
            try {
                String json = objectMapper.writeValueAsString(result);
                redisOps.set(cacheKey, json, ttlSeconds);
            } catch (JsonProcessingException e) {
                log.warn("缓存统计数据失败: key={}, error={}", key, e.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 清除指定缓存
     */
    public void evict(String key) {
        redisOps.delete(CACHE_PREFIX + key);
    }

    /**
     * 清除所有统计缓存
     */
    public void evictAll() {
        // 由于使用SCAN可能较慢，这里只清除已知的key
        String[] keys = {"overview", "dashboard", "posts", "users", "interactions", 
                "trend_7", "trend_14", "trend_30"};
        for (String key : keys) {
            evict(key);
        }
    }
}
