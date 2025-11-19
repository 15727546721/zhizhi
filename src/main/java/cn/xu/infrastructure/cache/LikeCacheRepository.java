package cn.xu.infrastructure.cache;

import cn.xu.domain.like.model.LikeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 点赞缓存仓储
 * 处理点赞相关的缓存操作
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LikeCacheRepository {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String LIKE_COUNT_KEY_PREFIX = "like:count:";
    private static final String USER_LIKE_KEY_PREFIX = "user:like:";
    private static final int DEFAULT_CACHE_TTL = 3600; // 1小时（用于用户关系缓存）
    // 计数类数据不应该过期，使用一个很长的过期时间（30天）作为兜底
    private static final int COUNT_CACHE_TTL = 30 * 24 * 3600; // 30天
    
    /**
     * 获取目标的点赞数
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    public Long getLikeCount(Long targetId, LikeType type) {
        String key = buildLikeCountKey(targetId, type);
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            }
            return value != null ? Long.parseLong(value.toString()) : null; // 返回null表示缓存未命中
        } catch (Exception e) {
            log.error("获取点赞数失败 - key: {}", key, e);
            return null; // 返回null表示缓存未命中
        }
    }
    
    /**
     * 增加目标的点赞数
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @param delta 增量
     * @return 增加后的点赞数
     */
    public Long incrementLikeCount(Long targetId, LikeType type, long delta) {
        String key = buildLikeCountKey(targetId, type);
        try {
            Long newValue = redisTemplate.opsForValue().increment(key, delta);
            // 计数类数据使用长期过期时间，避免数据丢失
            redisTemplate.expire(key, COUNT_CACHE_TTL, TimeUnit.SECONDS);
            return newValue != null ? newValue : 0L;
        } catch (Exception e) {
            log.error("增加点赞数失败 - key: {}", key, e);
            return 0L;
        }
    }
    
    /**
     * 设置目标的点赞数
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @param count 点赞数
     */
    public void setLikeCount(Long targetId, LikeType type, Long count) {
        String key = buildLikeCountKey(targetId, type);
        try {
            // 计数类数据使用长期过期时间，避免数据丢失
            redisTemplate.opsForValue().set(key, count, COUNT_CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("设置点赞数失败 - key: {}", key, e);
        }
    }
    
    /**
     * 删除目标的点赞数缓存
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void deleteLikeCount(Long targetId, LikeType type) {
        String key = buildLikeCountKey(targetId, type);
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除点赞数缓存失败 - key: {}", key, e);
        }
    }
    
    /**
     * 记录用户点赞关系
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void addUserLikeRelation(Long userId, Long targetId, LikeType type) {
        String key = buildUserLikeKey(userId);
        String value = buildLikeRelationValue(targetId, type);
        try {
            redisTemplate.opsForSet().add(key, value);
            // 设置过期时间
            redisTemplate.expire(key, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("记录用户点赞关系失败 - key: {}, value: {}", key, value, e);
        }
    }
    
    /**
     * 移除用户点赞关系
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void removeUserLikeRelation(Long userId, Long targetId, LikeType type) {
        String key = buildUserLikeKey(userId);
        String value = buildLikeRelationValue(targetId, type);
        try {
            redisTemplate.opsForSet().remove(key, value);
        } catch (Exception e) {
            log.error("移除用户点赞关系失败 - key: {}, value: {}", key, value, e);
        }
    }
    
    /**
     * 批量检查用户是否点赞了指定目标
     * 
     * @param userId 用户ID
     * @param targetIds 目标ID列表
     * @param type 点赞类型
     * @return 点赞状态Map，只包含已点赞的记录，key为目标ID，value为true
     */
    public java.util.Map<Long, Boolean> batchCheckUserLikeRelations(Long userId, java.util.List<Long> targetIds, LikeType type) {
        if (userId == null || targetIds == null || targetIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        
        java.util.Map<Long, Boolean> result = new java.util.HashMap<>();
        String key = buildUserLikeKey(userId);
        
        try {
            // 批量查询用户的所有点赞关系
            // 注意：由于Redis缓存可能是部分加载（Partial Cache），即只缓存了用户点赞的部分记录
            // 所以这里不能因为Redis中没有找到就认为用户没有点赞（返回false）
            // 只能确认"Redis中存在的肯定是已点赞的"（返回true）
            // 对于Redis中不存在的，需要交给上层去查数据库确认
            java.util.Set<Object> userLikedValues = redisTemplate.opsForSet().members(key);
            
            if (userLikedValues != null && !userLikedValues.isEmpty()) {
                // 缓存中有数据，检查每个目标是否被用户点赞
                for (Long targetId : targetIds) {
                    String valueToCheck = buildLikeRelationValue(targetId, type);
                    if (userLikedValues.contains(valueToCheck)) {
                        result.put(targetId, true);
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("批量检查用户点赞关系失败 - userId: {}, targetIds: {}, type: {}", userId, targetIds, type, e);
            // 异常时返回空Map，让上层去查数据库降级
            return new java.util.HashMap<>();
        }
    }
    
    /**
     * 检查用户是否点赞了目标
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 是否点赞
     */
    public boolean checkUserLikeRelation(Long userId, Long targetId, LikeType type) {
        String key = buildUserLikeKey(userId);
        String value = buildLikeRelationValue(targetId, type);
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("检查用户点赞关系失败 - key: {}, value: {}", key, value, e);
            return false;
        }
    }
    
    /**
     * 构建点赞数键
     */
    private String buildLikeCountKey(Long targetId, LikeType type) {
        return LIKE_COUNT_KEY_PREFIX + type.getRedisKeyName() + ":" + targetId;
    }
    
    /**
     * 构建用户点赞关系键
     */
    private String buildUserLikeKey(Long userId) {
        return USER_LIKE_KEY_PREFIX + userId;
    }
    
    /**
     * 构建点赞关系值
     */
    private String buildLikeRelationValue(Long targetId, LikeType type) {
        return type.getRedisKeyName() + ":" + targetId;
    }
}