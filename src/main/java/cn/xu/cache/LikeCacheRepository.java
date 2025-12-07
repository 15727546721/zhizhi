package cn.xu.cache;

import cn.xu.model.entity.Like.LikeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 点赞缓存仓储
 * <p>处理点赞相关的缓存操作</p>
 * <p>继承BaseCacheRepository复用通用方法，减少重复代码</p>

 */
@Slf4j
@Repository
public class LikeCacheRepository extends BaseCacheRepository {
    
    /**
     * 获取目标的点赞数
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    public Long getLikeCount(Long targetId, LikeType type) {
        String key = RedisKeyManager.likeCountKey(type, targetId);
        return getCount(key);
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
        String key = RedisKeyManager.likeCountKey(type, targetId);
        return incrementCount(key, delta, RedisKeyManager.COUNT_TTL);
    }
    
    /**
     * 设置目标的点赞数
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @param count 点赞数
     */
    public void setLikeCount(Long targetId, LikeType type, Long count) {
        String key = RedisKeyManager.likeCountKey(type, targetId);
        setCount(key, count, RedisKeyManager.COUNT_TTL);
    }
    
    /**
     * 删除目标的点赞数缓存
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void deleteLikeCount(Long targetId, LikeType type) {
        String key = RedisKeyManager.likeCountKey(type, targetId);
        deleteCache(key);
    }
    
    /**
     * 记录用户点赞关系
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void addUserLikeRelation(Long userId, Long targetId, LikeType type) {
        String key = RedisKeyManager.likeRelationKey(userId, type);
        String value = buildLikeRelationValue(targetId, type);
        addToSet(key, new Object[]{value}, RedisKeyManager.RELATION_TTL);
    }
    
    /**
     * 移除用户点赞关系
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void removeUserLikeRelation(Long userId, Long targetId, LikeType type) {
        String key = RedisKeyManager.likeRelationKey(userId, type);
        String value = buildLikeRelationValue(targetId, type);
        removeFromSet(key, value);
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
        String key = RedisKeyManager.likeRelationKey(userId, type);
        
        try {
            // 批量查询用户的所有点赞关系
            // 注意：由于Redis缓存可能是部分加载（Partial Cache），即只缓存了用户点赞的部分记录
            // 所以这里不能因为Redis中没有找到就认为用户没有点赞（返回false）
            // 只能确认Redis中存在的肯定是已点赞的（返回true）
            // 对于Redis中不存在的，需要交给上层去查数据库确认
            java.util.Set<Object> userLikedValues = getSetMembers(key);
            
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
        String key = RedisKeyManager.likeRelationKey(userId, type);
        String value = buildLikeRelationValue(targetId, type);
        return isMemberOfSet(key, value);
    }
    
    /**
     * 构建点赞关系Value
     */
    private String buildLikeRelationValue(Long targetId, LikeType type) {
        return type.getRedisKeyName() + ":" + targetId;
    }
}
