package cn.xu.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 收藏缓存仓储
 * <p>处理收藏相关的缓存操作</p>
 * <p>继承BaseCacheRepository复用通用方法，减少重复代码</p>
 
 */
@Slf4j
@Repository
public class FavoriteCacheRepository extends BaseCacheRepository {
    
    /**
     * 获取目标的收藏数
     * 
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 收藏数
     */
    public Long getFavoriteCount(Long targetId, String targetType) {
        String key = RedisKeyManager.favoriteCountKey(targetType, targetId);
        return getCount(key);
    }
    
    /**
     * 增加目标的收藏数
     * 
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @param delta      增量
     * @return 增加后的收藏数
     */
    public Long incrementFavoriteCount(Long targetId, String targetType, long delta) {
        String key = RedisKeyManager.favoriteCountKey(targetType, targetId);
        return incrementCount(key, delta, RedisKeyManager.COUNT_TTL);
    }
    
    /**
     * 删除目标的收藏数缓存
     * 
     * @param targetId   目标ID
     * @param targetType 目标类型
     */
    public void deleteFavoriteCount(Long targetId, String targetType) {
        String key = RedisKeyManager.favoriteCountKey(targetType, targetId);
        deleteCache(key);
    }

    /**
     * 设置目标的收藏数
     * 
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @param count      收藏数
     */
    public void setFavoriteCount(Long targetId, String targetType, Long count) {
        String key = RedisKeyManager.favoriteCountKey(targetType, targetId);
        setCount(key, count, RedisKeyManager.COUNT_TTL);
    }
    
    /**
     * 记录用户收藏关系
     * 
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     */
    public void addUserFavoriteRelation(Long userId, Long targetId, String targetType) {
        String key = RedisKeyManager.userFavoriteKey(userId);
        String value = targetType + ":" + targetId;
        addToSet(key, new Object[]{value}, RedisKeyManager.DEFAULT_TTL);
    }
    
    /**
     * 移除用户收藏关系
     * 
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     */
    public void removeUserFavoriteRelation(Long userId, Long targetId, String targetType) {
        String key = RedisKeyManager.userFavoriteKey(userId);
        String value = targetType + ":" + targetId;
        removeFromSet(key, value);
    }
    
    /**
     * 检查用户是否收藏了目标
     * 
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 是否收藏
     */
    public Boolean checkUserFavoriteRelation(Long userId, Long targetId, String targetType) {
        String key = RedisKeyManager.userFavoriteKey(userId);
        String value = targetType + ":" + targetId;
        return isMemberOfSet(key, value) ? true : null;
    }
    
    /**
     * 更新用户收藏关系
     * 
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @param isFavorite 是否收藏
     */
    public void updateUserFavoriteRelation(Long userId, Long targetId, String targetType, boolean isFavorite) {
        if (isFavorite) {
            addUserFavoriteRelation(userId, targetId, targetType);
        } else {
            removeUserFavoriteRelation(userId, targetId, targetType);
        }
        log.debug("[缓存] 更新用户收藏关系成功 - userId: {}, targetId: {}, targetType: {}, isFavorite: {}", 
                userId, targetId, targetType, isFavorite);
    }

    /**
     * 获取用户收藏的目标数量
     * 
     * @param userId     用户ID
     * @param targetType 目标类型
     * @return 收藏数量
     */
    public Long getUserFavoriteCount(Long userId, String targetType) {
        String countKey = RedisKeyManager.userFavoriteCountKey(userId, targetType);
        return getCount(countKey);
    }
    
    /**
     * 增加用户收藏的目标数量
     * 
     * @param userId     用户ID
     * @param targetType 目标类型
     */
    public void incrementUserFavoriteCount(Long userId, String targetType) {
        String countKey = RedisKeyManager.userFavoriteCountKey(userId, targetType);
        incrementCount(countKey, 1, RedisKeyManager.DEFAULT_TTL);
    }
    
    /**
     * 减少用户收藏的目标数量
     * 
     * @param userId     用户ID
     * @param targetType 目标类型
     */
    public void decrementUserFavoriteCount(Long userId, String targetType) {
        String countKey = RedisKeyManager.userFavoriteCountKey(userId, targetType);
        Long newValue = incrementCount(countKey, -1, RedisKeyManager.DEFAULT_TTL);
        // 确保数量不为负数
        if (newValue < 0) {
            setCount(countKey, 0L, RedisKeyManager.DEFAULT_TTL);
        }
    }

    /**
     * 设置用户收藏的目标数量
     * 
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param count      收藏数量
     */
    public void setUserFavoriteCount(Long userId, String targetType, Long count) {
        String countKey = RedisKeyManager.userFavoriteCountKey(userId, targetType);
        setCount(countKey, count, RedisKeyManager.DEFAULT_TTL);
    }

    /**
     * 增加收藏夹内容数量
     * 
     * @param folderId 收藏夹ID
     */
    public void incrementFolderContentCount(Long folderId) {
        String key = RedisKeyManager.favoriteFolderContentCountKey(folderId);
        incrementCount(key, 1, RedisKeyManager.DEFAULT_TTL);
    }
    
    /**
     * 减少收藏夹内容数量
     * 
     * @param folderId 收藏夹ID
     */
    public void decrementFolderContentCount(Long folderId) {
        String key = RedisKeyManager.favoriteFolderContentCountKey(folderId);
        Long newValue = incrementCount(key, -1, RedisKeyManager.DEFAULT_TTL);
        // 确保数量不为负数
        if (newValue < 0) {
            setCount(key, 0L, RedisKeyManager.DEFAULT_TTL);
        }
    }
    
    /**
     * 删除收藏夹内容数量缓存
     * 
     * @param folderId 收藏夹ID
     */
    public void deleteFolderContentCount(Long folderId) {
        String key = RedisKeyManager.favoriteFolderContentCountKey(folderId);
        deleteCache(key);
    }
}
