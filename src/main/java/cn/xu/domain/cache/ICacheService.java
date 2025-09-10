package cn.xu.domain.cache;

import java.util.Map;

/**
 * 缓存服务接口
 * 遵循DDD原则，定义领域层缓存操作接口
 */
public interface ICacheService {
    
    /**
     * 获取缓存中的计数值
     * @param key 键
     * @return 计数值
     */
    Long getCount(String key);
    
    /**
     * 批量获取缓存中的计数值
     * @param keys 键列表
     * @return 计数值映射
     */
    Map<String, Long> batchGetCount(String... keys);
    
    /**
     * 设置缓存中的计数值
     * @param key 键
     * @param count 计数值
     */
    void setCount(String key, Long count);
    
    /**
     * 增加缓存中的计数值
     * @param key 键
     * @param delta 增量
     * @return 增加后的值
     */
    Long incrementCount(String key, long delta);
    
    /**
     * 删除缓存中的键
     * @param key 键
     */
    void delete(String key);
    
    /**
     * 批量删除缓存中的键
     * @param keys 键列表
     */
    void batchDelete(String... keys);
    
    /**
     * 检查键是否存在
     * @param key 键
     * @return 是否存在
     */
    boolean exists(String key);
}