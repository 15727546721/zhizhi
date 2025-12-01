package cn.xu.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 收藏缓存仓储
 * 处理收藏相关的缓存操作
 * 
 * <p>继承BaseCacheRepository复用通用方法，减少重复代码
 * 
 * @author zhizhi
 * @since 2025-11-23
 */
@Slf4j
@Repository
public class FavoriteCacheRepository extends BaseCacheRepository {
    
    // Redis Key前缀
    private static final String FAVORITE_COUNT_KEY_PREFIX = "favorite:count:";
    private static final String USER_FAVORITE_KEY_PREFIX = "user:favorite:";
    private static final String FOLDER_CONTENT_COUNT_PREFIX = "favorite:folder:content:";
    
    // TTL配置
    private static final int DEFAULT_CACHE_TTL = 3600; // 1小时（用户关系缓存）
    private static final int COUNT_CACHE_TTL = 30 * 24 * 3600; // 30天（计数类数据）
    
    /**
     * 获取目标的收藏数
     * 
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 收藏数
     */
    public Long getFavoriteCount(Long targetId, String targetType) {
        String key = buildFavoriteCountKey(targetId, targetType);
        return getCount(key);
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
        return incrementCount(key, delta, COUNT_CACHE_TTL);
    }
    
    /**
     * 删除目标的收藏数缓存
     * 
     * @param targetId 目标ID
     * @param targetType 目标类型
     */
    public void deleteFavoriteCount(Long targetId, String targetType) {
        String key = buildFavoriteCountKey(targetId, targetType);
        deleteCache(key);
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
        setCount(key, count, COUNT_CACHE_TTL);
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
        addToSet(key, new Object[]{value}, DEFAULT_CACHE_TTL);
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
        removeFromSet(key, value);
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
        return isMemberOfSet(key, value) ? true : null;
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
        if (isFavorite) {
            addUserFavoriteRelation(userId, targetId, targetType);
        } else {
            removeUserFavoriteRelation(userId, targetId, targetType);
        }
        log.debug("更新用户收藏关系缓存成功，userId={}, targetId={}, targetType={}, isFavorite={}", 
                userId, targetId, targetType, isFavorite);
    }

    /**
     * 获取用户收藏的项目数量
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     * @return 收藏数量
     */
    public Long getUserFavoriteCount(Long userId, String targetType) {
        String countKey = USER_FAVORITE_KEY_PREFIX + "count:" + userId + ":" + targetType;
        return getCount(countKey);
    }
    
    /**
     * 增加用户收藏的项目数量
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     */
    public void incrementUserFavoriteCount(Long userId, String targetType) {
        String countKey = USER_FAVORITE_KEY_PREFIX + "count:" + userId + ":" + targetType;
        incrementCount(countKey, 1, DEFAULT_CACHE_TTL);
    }
    
    /**
     * 减少用户收藏的项目数量
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     */
    public void decrementUserFavoriteCount(Long userId, String targetType) {
        String countKey = USER_FAVORITE_KEY_PREFIX + "count:" + userId + ":" + targetType;
        Long newValue = incrementCount(countKey, -1, DEFAULT_CACHE_TTL);
        // 确保数量不为负数
        if (newValue < 0) {
            setCount(countKey, 0L, DEFAULT_CACHE_TTL);
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
        String countKey = USER_FAVORITE_KEY_PREFIX + "count:" + userId + ":" + targetType;
        setCount(countKey, count, DEFAULT_CACHE_TTL);
    }

    /**
     * 增加收藏夹内容数量
     * 
     * @param folderId 收藏夹ID
     */
    public void incrementFolderContentCount(Long folderId) {
        String key = buildFolderContentCountKey(folderId);
        incrementCount(key, 1, DEFAULT_CACHE_TTL);
    }
    
    /**
     * 减少收藏夹内容数量
     * 
     * @param folderId 收藏夹ID
     */
    public void decrementFolderContentCount(Long folderId) {
        String key = buildFolderContentCountKey(folderId);
        Long newValue = incrementCount(key, -1, DEFAULT_CACHE_TTL);
        // 确保数量不为负数
        if (newValue < 0) {
            setCount(key, 0L, DEFAULT_CACHE_TTL);
        }
    }
    
    /**
     * 删除收藏夹内容数量缓存
     * 
     * @param folderId 收藏夹ID
     */
    public void deleteFolderContentCount(Long folderId) {
        String key = buildFolderContentCountKey(folderId);
        deleteCache(key);
    }
    
    // ==================== Key构建方法 ====================
    
    /**
     * 构建收藏数Key
     */
    private String buildFavoriteCountKey(Long targetId, String targetType) {
        return FAVORITE_COUNT_KEY_PREFIX + targetType + ":" + targetId;
    }
    
    /**
     * 构建用户收藏关系Key
     */
    private String buildUserFavoriteKey(Long userId) {
        return USER_FAVORITE_KEY_PREFIX + userId;
    }
    
    /**
     * 构建收藏关系Value
     */
    private String buildFavoriteRelationValue(Long targetId, String targetType) {
        return targetType + ":" + targetId;
    }
    
    /**
     * 构建收藏夹内容数量Key
     */
    private String buildFolderContentCountKey(Long folderId) {
        return FOLDER_CONTENT_COUNT_PREFIX + folderId;
    }
}