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
    private static final int DEFAULT_CACHE_TTL = 3600; // 1小时
    
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
            return value != null ? Long.parseLong(value.toString()) : 0L;
        } catch (Exception e) {
            log.error("获取点赞数失败 - key: {}", key, e);
            return 0L;
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
            // 设置过期时间
            redisTemplate.expire(key, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
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
            redisTemplate.opsForValue().set(key, count, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("设置点赞数失败 - key: {}", key, e);
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
        return LIKE_COUNT_KEY_PREFIX + type.getCode() + ":" + targetId;
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
        return type.getCode() + ":" + targetId;
    }
}