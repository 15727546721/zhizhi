package cn.xu.cache.core;

import cn.xu.cache.core.RedisOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * 缓存Repository基类
 * <p>提供通用的Redis操作方法，减少重复代码</p>
 * <p>所有CacheRepository应继承此类，通过RedisOperations访问Redis</p>
 *
 * <p>提供的通用方法：</p>
 * <ul>
 *   <li>计数操作：getCount、setCount、incrementCount</li>
 *   <li>Set操作：addToSet、removeFromSet、isMemberOfSet</li>
 *   <li>通用操作：deleteCache、hasKey、setValue</li>
 *   <li>类型转换：convertToLong、convertToString</li>
 * </ul>
 */
@Slf4j
public abstract class BaseCacheRepository {
    
    @Autowired
    protected RedisOperations redisOps;
    
    // ==================== 计数操作 ====================
    
    /**
     * 获取计数值
     * 
     * @param key Redis key
     * @return 计数值，缓存未命中返回null
     */
    protected Long getCount(String key) {
        Object value = redisOps.get(key);
        return convertToLong(value);
    }
    
    /**
     * 设置计数值
     * 
     * @param key   Redis key
     * @param count 计数值
     * @param ttl   过期时间（秒）
     */
    protected void setCount(String key, Long count, int ttl) {
        redisOps.set(key, count, ttl);
        log.debug("[缓存] 设置计数: key={}, count={}, ttl={}s", key, count, ttl);
    }
    
    /**
     * 增加计数值
     * 
     * @param key   Redis key
     * @param delta 增量（可为负数）
     * @param ttl   过期时间（秒）
     * @return 增加后的值
     */
    protected Long incrementCount(String key, long delta, int ttl) {
        long newValue = redisOps.increment(key, delta);
        // 只在刚创建时设置过期时间
        if (newValue == delta) {
            redisOps.expire(key, ttl);
        }
        log.debug("[缓存] 增加计数: key={}, delta={}, newValue={}", key, delta, newValue);
        return newValue;
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
        long addedCount = redisOps.sAdd(key, values);
        // 只在首次添加时设置过期时间
        if (addedCount > 0) {
            long currentSize = redisOps.sSize(key);
            if (currentSize == addedCount) {
                redisOps.expire(key, ttl);
            }
        }
        log.debug("[缓存] 添加到Set: key={}, count={}", key, values.length);
    }
    
    /**
     * 从Set中移除元素
     * 
     * @param key Redis key
     * @param value 要移除的值
     */
    protected void removeFromSet(String key, Object value) {
        redisOps.sRemove(key, value);
        log.debug("[缓存] 从Set移除: key={}", key);
    }
    
    /**
     * 检查Set中是否存在元素
     * 
     * @param key Redis key
     * @param value 要检查的值
     * @return 是否存在
     */
    protected boolean isMemberOfSet(String key, Object value) {
        return redisOps.sIsMember(key, value);
    }
    
    /**
     * 获取Set的所有成员
     * 
     * @param key Redis key
     * @return Set成员集合
     */
    protected Set<Object> getSetMembers(String key) {
        return redisOps.sMembers(key);
    }
    
    // ==================== 通用操作 ====================
    
    /**
     * 删除缓存
     * 
     * @param key Redis key
     */
    protected void deleteCache(String key) {
        redisOps.delete(key);
        log.debug("[缓存] 删除: key={}", key);
    }
    
    /**
     * 批量删除缓存
     * 
     * @param keys Redis keys
     */
    protected void deleteCacheBatch(java.util.Collection<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            redisOps.delete(keys);
            log.debug("[缓存] 批量删除: count={}", keys.size());
        }
    }
    
    /**
     * 检查key是否存在
     * 
     * @param key Redis key
     * @return 是否存在
     */
    protected boolean hasKey(String key) {
        return redisOps.hasKey(key);
    }
    
    /**
     * 设置简单值
     * 
     * @param key   Redis key
     * @param value 值
     * @param ttl   过期时间（秒）
     */
    protected void setValue(String key, Object value, int ttl) {
        redisOps.set(key, value, ttl);
        log.debug("[缓存] 设置值: key={}", key);
    }
    
    /**
     * 获取简单值
     * 
     * @param key Redis key
     * @return 值
     */
    protected Object getValue(String key) {
        return redisOps.get(key);
    }
    
    /**
     * 设置过期时间
     * 
     * @param key Redis key
     * @param ttl 过期时间（秒）
     */
    protected void expire(String key, int ttl) {
        redisOps.expire(key, ttl);
        log.debug("[缓存] 设置过期时间: key={}, ttl={}s", key, ttl);
    }
    
    /**
     * 获取底层RedisOperations（供子类特殊场景使用）
     */
    protected RedisOperations getRedisOps() {
        return redisOps;
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