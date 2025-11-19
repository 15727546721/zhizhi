package cn.xu.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 收藏缓存仓储
 * 处理收藏相关的缓存操作
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class FavoriteCacheRepository {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String FAVORITE_COUNT_KEY_PREFIX = "favorite:count:";
    private static final String USER_FAVORITE_KEY_PREFIX = "user:favorite:";
    private static final String FOLDER_CONTENT_COUNT_PREFIX = "favorite:folder:content:";
    private static final int DEFAULT_CACHE_TTL = 3600; // 1小时（用于用户关系缓存）
    // 计数类数据不应该过期，使用一个很长的过期时间（30天）作为兜底
    private static final int COUNT_CACHE_TTL = 30 * 24 * 3600; // 30天
    
    /**
     * 获取目标的收藏数
     * 
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 收藏数
     */
    public Long getFavoriteCount(Long targetId, String targetType) {
        String key = buildFavoriteCountKey(targetId, targetType);
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            }
            return value != null ? Long.parseLong(value.toString()) : null; // 返回null表示缓存未命中
        } catch (Exception e) {
            log.error("获取收藏数失败 - key: {}", key, e);
            return null; // 返回null表示缓存未命中
        }
    }
    
    /**
     * 增加目标的收藏数
     * 
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param delta 增量
     * @return 增加后的收藏数
     */
    public Long incrementFavoriteCount(Long targetId, String targetType, long delta) {
        String key = buildFavoriteCountKey(targetId, targetType);
        try {
            Long newValue = redisTemplate.opsForValue().increment(key, delta);
            // 计数类数据使用长期过期时间，避免数据丢失
            redisTemplate.expire(key, COUNT_CACHE_TTL, TimeUnit.SECONDS);
            return newValue != null ? newValue : 0L;
        } catch (Exception e) {
            log.error("增加收藏数失败 - key: {}", key, e);
            return 0L;
        }
    }
    
    /**
     * 删除目标的收藏数缓存
     * 
     * @param targetId 目标ID
     * @param targetType 目标类型
     */
    public void deleteFavoriteCount(Long targetId, String targetType) {
        String key = buildFavoriteCountKey(targetId, targetType);
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除收藏数缓存失败 - key: {}", key, e);
        }
    }

    /**
     * 记录用户收藏关系
     * 
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param count 收藏数
     */
    public void setFavoriteCount(Long targetId, String targetType, Long count) {
        String key = buildFavoriteCountKey(targetId, targetType);
        try {
            // 计数类数据使用长期过期时间，避免数据丢失
            redisTemplate.opsForValue().set(key, count, COUNT_CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("设置收藏数失败 - key: {}", key, e);
        }
    }
    
    /**
     * 记录用户收藏关系
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     */
    public void addUserFavoriteRelation(Long userId, Long targetId, String targetType) {
        String key = buildUserFavoriteKey(userId);
        String value = buildFavoriteRelationValue(targetId, targetType);
        try {
            redisTemplate.opsForSet().add(key, value);
            // 设置过期时间
            redisTemplate.expire(key, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("记录用户收藏关系失败 - key: {}, value: {}", key, value, e);
        }
    }
    
    /**
     * 移除用户收藏关系
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     */
    public void removeUserFavoriteRelation(Long userId, Long targetId, String targetType) {
        String key = buildUserFavoriteKey(userId);
        String value = buildFavoriteRelationValue(targetId, targetType);
        try {
            redisTemplate.opsForSet().remove(key, value);
        } catch (Exception e) {
            log.error("移除用户收藏关系失败 - key: {}, value: {}", key, value, e);
        }
    }
    
    /**
     * 检查用户是否收藏了目标
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 是否收藏
     */
    public Boolean checkUserFavoriteRelation(Long userId, Long targetId, String targetType) {
        String key = buildUserFavoriteKey(userId);
        String value = buildFavoriteRelationValue(targetId, targetType);
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("检查用户收藏关系失败 - key: {}, value: {}", key, value, e);
            return null; // 返回null表示缓存查询失败
        }
    }
    
    /**
     * 更新用户收藏关系
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param isFavorite 是否收藏
     */
    public void updateUserFavoriteRelation(Long userId, Long targetId, String targetType, boolean isFavorite) {
        try {
            if (isFavorite) {
                addUserFavoriteRelation(userId, targetId, targetType);
            } else {
                removeUserFavoriteRelation(userId, targetId, targetType);
            }
            log.debug("更新用户收藏关系缓存成功，userId={}, targetId={}, targetType={}, isFavorite={}", 
                    userId, targetId, targetType, isFavorite);
        } catch (Exception e) {
            log.error("更新用户收藏关系缓存失败，userId={}, targetId={}, targetType={}, isFavorite={}", 
                    userId, targetId, targetType, isFavorite, e);
        }
    }

    /**
     * 获取用户收藏的项目数量
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     * @return 收藏数量
     */
    public Long getUserFavoriteCount(Long userId, String targetType) {
        try {
            // 使用独立的缓存键存储用户收藏数量
            String countKey = USER_FAVORITE_KEY_PREFIX + "count:" + userId + ":" + targetType;
            Object value = redisTemplate.opsForValue().get(countKey);
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            }
            return value != null ? Long.parseLong(value.toString()) : null; // 返回null表示缓存未命中
        } catch (Exception e) {
            log.error("获取用户收藏数量缓存失败，userId={}, targetType={}", userId, targetType, e);
            return null;
        }
    }
    
    /**
     * 增加用户收藏的项目数量
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     */
    public void incrementUserFavoriteCount(Long userId, String targetType) {
        try {
            String countKey = USER_FAVORITE_KEY_PREFIX + "count:" + userId + ":" + targetType;
            redisTemplate.opsForValue().increment(countKey, 1);
            redisTemplate.expire(countKey, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("增加用户收藏数量缓存成功，userId={}, targetType={}", userId, targetType);
        } catch (Exception e) {
            log.error("增加用户收藏数量缓存失败，userId={}, targetType={}", userId, targetType, e);
        }
    }
    
    /**
     * 减少用户收藏的项目数量
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     */
    public void decrementUserFavoriteCount(Long userId, String targetType) {
        try {
            String countKey = USER_FAVORITE_KEY_PREFIX + "count:" + userId + ":" + targetType;
            Long newValue = redisTemplate.opsForValue().increment(countKey, -1);
            // 确保数量不为负数
            if (newValue != null && newValue < 0) {
                redisTemplate.opsForValue().set(countKey, 0, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            } else {
                redisTemplate.expire(countKey, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            }
            log.debug("减少用户收藏数量缓存成功，userId={}, targetType={}", userId, targetType);
        } catch (Exception e) {
            log.error("减少用户收藏数量缓存失败，userId={}, targetType={}", userId, targetType, e);
        }
    }

    /**
     * 设置用户收藏的项目数量
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param count 收藏数量
     */
    public void setUserFavoriteCount(Long userId, String targetType, Long count) {
        try {
            // 创建临时键用于存储用户收藏数量
            String countKey = USER_FAVORITE_KEY_PREFIX + "count:" + userId + ":" + targetType;
            redisTemplate.opsForValue().set(countKey, count, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("设置用户收藏数量缓存成功，userId={}, targetType={}, count={}", userId, targetType, count);
        } catch (Exception e) {
            log.error("设置用户收藏数量缓存失败，userId={}, targetType={}, count={}", userId, targetType, count, e);
        }
    }

    /**
     * 增加收藏夹内容数量
     * 
     * @param folderId 收藏夹ID
     */
    public void incrementFolderContentCount(Long folderId) {
        try {
            String key = buildFolderContentCountKey(folderId);
            redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("增加收藏夹内容数量缓存成功，folderId={}", folderId);
        } catch (Exception e) {
            log.error("增加收藏夹内容数量缓存失败，folderId={}", folderId, e);
        }
    }
    
    /**
     * 减少收藏夹内容数量
     * 
     * @param folderId 收藏夹ID
     */
    public void decrementFolderContentCount(Long folderId) {
        try {
            String key = buildFolderContentCountKey(folderId);
            Long newValue = redisTemplate.opsForValue().increment(key, -1);
            // 确保数量不为负数
            if (newValue != null && newValue < 0) {
                redisTemplate.opsForValue().set(key, 0, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            } else {
                redisTemplate.expire(key, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            }
            log.debug("减少收藏夹内容数量缓存成功，folderId={}", folderId);
        } catch (Exception e) {
            log.error("减少收藏夹内容数量缓存失败，folderId={}", folderId, e);
        }
    }
    
    /**
     * 删除收藏夹内容数量缓存
     * 
     * @param folderId 收藏夹ID
     */
    public void deleteFolderContentCount(Long folderId) {
        try {
            String key = buildFolderContentCountKey(folderId);
            redisTemplate.delete(key);
            log.debug("删除收藏夹内容数量缓存成功，folderId={}", folderId);
        } catch (Exception e) {
            log.error("删除收藏夹内容数量缓存失败，folderId={}", folderId, e);
        }
    }
    
    /**
     * 构建收藏数键
     */
    private String buildFavoriteCountKey(Long targetId, String targetType) {
        return FAVORITE_COUNT_KEY_PREFIX + targetType + ":" + targetId;
    }
    
    /**
     * 构建用户收藏关系键
     */
    private String buildUserFavoriteKey(Long userId) {
        return USER_FAVORITE_KEY_PREFIX + userId;
    }
    
    /**
     * 构建收藏关系值
     */
    private String buildFavoriteRelationValue(Long targetId, String targetType) {
        return targetType + ":" + targetId;
    }
    
    /**
     * 构建收藏夹内容数量缓存键
     */
    private String buildFolderContentCountKey(Long folderId) {
        return FOLDER_CONTENT_COUNT_PREFIX + folderId;
    }
}