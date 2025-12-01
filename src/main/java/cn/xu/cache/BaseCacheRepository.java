package cn.xu.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 缓存Repository基类
 * 提供通用的Redis操作方法，减少重复代码
 * 
 * <p>所有CacheRepository应继承此类，复用通用方法：
 * <ul>
 *   <li>计数操作：getCount、setCount、incrementCount</li>
 *   <li>Set操作：addToSet、removeFromSet、isMemberOfSet</li>
 *   <li>通用操作：deleteCache、hasKey、setValue</li>
 *   <li>类型转换：convertToLong、convertToString</li>
 * </ul>
 * 
 * @author zhizhi
 * @since 2025-11-23
 */
@Slf4j
public abstract class BaseCacheRepository {
    
    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;
    
    // ==================== 计数操作 ====================
    
    /**
     * 获取计数值
     * 
     * @param key Redis key
     * @return 计数值，缓存未命中返回null
     */
    protected Long getCount(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return convertToLong(value);
        } catch (Exception e) {
            log.error("[缓存] 获取计数失败 - key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 设置计数值
     * 
     * @param key Redis key
     * @param count 计数值
     * @param ttl 过期时间（秒）
     */
    protected void setCount(String key, Long count, int ttl) {
        try {
            redisTemplate.opsForValue().set(key, count, ttl, TimeUnit.SECONDS);
            log.debug("[缓存] 设置计数成功 - key: {}, count: {}, ttl: {}s", key, count, ttl);
        } catch (Exception e) {
            log.error("[缓存] 设置计数失败 - key: {}, count: {}", key, count, e);
        }
    }
    
    /**
     * 增加计数值
     * 
     * @param key Redis key
     * @param delta 增量（可为负数）
     * @param ttl 过期时间（秒）
     * @return 增加后的值
     */
    protected Long incrementCount(String key, long delta, int ttl) {
        try {
            Long newValue = redisTemplate.opsForValue().increment(key, delta);
            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
            log.debug("[缓存] 增加计数成功 - key: {}, delta: {}, newValue: {}", key, delta, newValue);
            return newValue != null ? newValue : 0L;
        } catch (Exception e) {
            log.error("[缓存] 增加计数失败 - key: {}, delta: {}", key, delta, e);
            return 0L;
        }
    }
    
    // ==================== Set操作 ====================
    
    /**
     * 添加元素到Set
     * 
     * @param key Redis key
     * @param values 要添加的值
     * @param ttl 过期时间（秒）
     */
    protected void addToSet(String key, Object[] values, int ttl) {
        try {
            redisTemplate.opsForSet().add(key, values);
            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
            log.debug("[缓存] 添加到Set成功 - key: {}, count: {}", key, values.length);
        } catch (Exception e) {
            log.error("[缓存] 添加到Set失败 - key: {}", key, e);
        }
    }
    
    /**
     * 从Set中移除元素
     * 
     * @param key Redis key
     * @param value 要移除的值
     */
    protected void removeFromSet(String key, Object value) {
        try {
            redisTemplate.opsForSet().remove(key, value);
            log.debug("[缓存] 从Set移除成功 - key: {}, value: {}", key, value);
        } catch (Exception e) {
            log.error("[缓存] 从Set移除失败 - key: {}, value: {}", key, value, e);
        }
    }
    
    /**
     * 检查Set中是否存在元素
     * 
     * @param key Redis key
     * @param value 要检查的值
     * @return 是否存在
     */
    protected boolean isMemberOfSet(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("[缓存] 检查Set成员失败 - key: {}, value: {}", key, value, e);
            return false;
        }
    }
    
    /**
     * 获取Set的所有成员
     * 
     * @param key Redis key
     * @return Set成员集合
     */
    protected java.util.Set<Object> getSetMembers(String key) {
        try {
            java.util.Set<Object> members = redisTemplate.opsForSet().members(key);
            return members != null ? members : java.util.Collections.emptySet();
        } catch (Exception e) {
            log.error("[缓存] 获取Set成员失败 - key: {}", key, e);
            return java.util.Collections.emptySet();
        }
    }
    
    // ==================== 通用操作 ====================
    
    /**
     * 删除缓存
     * 
     * @param key Redis key
     */
    protected void deleteCache(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("[缓存] 删除缓存成功 - key: {}", key);
        } catch (Exception e) {
            log.error("[缓存] 删除缓存失败 - key: {}", key, e);
        }
    }
    
    /**
     * 批量删除缓存
     * 
     * @param keys Redis keys
     */
    protected void deleteCacheBatch(java.util.Collection<String> keys) {
        try {
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("[缓存] 批量删除缓存成功 - count: {}", keys.size());
            }
        } catch (Exception e) {
            log.error("[缓存] 批量删除缓存失败", e);
        }
    }
    
    /**
     * 检查key是否存在
     * 
     * @param key Redis key
     * @return 是否存在
     */
    protected boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("[缓存] 检查key存在性失败 - key: {}", key, e);
            return false;
        }
    }
    
    /**
     * 设置简单值
     * 
     * @param key Redis key
     * @param value 值
     * @param ttl 过期时间（秒）
     */
    protected void setValue(String key, Object value, int ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
            log.debug("[缓存] 设置值成功 - key: {}", key);
        } catch (Exception e) {
            log.error("[缓存] 设置值失败 - key: {}", key, e);
        }
    }
    
    /**
     * 获取简单值
     * 
     * @param key Redis key
     * @return 值
     */
    protected Object getValue(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("[缓存] 获取值失败 - key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 设置过期时间
     * 
     * @param key Redis key
     * @param ttl 过期时间（秒）
     */
    protected void expire(String key, int ttl) {
        try {
            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
            log.debug("[缓存] 设置过期时间成功 - key: {}, ttl: {}s", key, ttl);
        } catch (Exception e) {
            log.error("[缓存] 设置过期时间失败 - key: {}", key, e);
        }
    }
    
    // ==================== 类型转换 ====================
    
    /**
     * 安全转换为Long
     * 
     * @param value 原始值
     * @return Long值，失败返回null
     */
    protected Long convertToLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("[缓存] Long类型转换失败 - value: {}", value);
            return null;
        }
    }
    
    /**
     * 安全转换为String
     * 
     * @param value 原始值
     * @return String值
     */
    protected String convertToString(Object value) {
        return value != null ? value.toString() : null;
    }
    
    /**
     * 安全转换为Integer
     * 
     * @param value 原始值
     * @return Integer值，失败返回null
     */
    protected Integer convertToInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("[缓存] Integer类型转换失败 - value: {}", value);
            return null;
        }
    }
}
