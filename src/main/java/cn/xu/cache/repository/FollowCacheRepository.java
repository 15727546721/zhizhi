package cn.xu.cache.repository;

import cn.xu.cache.core.BaseCacheRepository;
import cn.xu.cache.core.RedisKeyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 关注缓存仓储
 * <p>处理关注相关的缓存操作</p>
 * <p>继承BaseCacheRepository复用通用方法，减少重复代码</p>
 
 */
@Slf4j
@Repository
public class FollowCacheRepository extends BaseCacheRepository {

    /**
     * 缓存用户的关注列表
     * 
     * @param followerId   关注者ID
     * @param followingIds 关注的用户ID列表
     */
    public void cacheFollowingList(Long followerId, List<Long> followingIds) {
        String redisKey = RedisKeyManager.followFollowingKey(followerId);
        
        deleteCache(redisKey); // 先清空旧数据
        
        if (followingIds != null && !followingIds.isEmpty()) {
            addToSet(redisKey, followingIds.toArray(), RedisKeyManager.RELATION_TTL);
            log.debug("[缓存] 缓存关注列表成功 - key: {}, size: {}", redisKey, followingIds.size());
        } else {
            // 缓存空结果，防止缓存穿透
            setValue(redisKey + ":empty", "1", RedisKeyManager.EMPTY_RESULT_TTL);
        }
    }

    /**
     * 从缓存获取用户的关注列表
     * 
     * @param followerId 关注者ID
     * @return 关注的用户ID列表
     */
    public List<Long> getFollowingListFromCache(Long followerId) {
        String redisKey = RedisKeyManager.followFollowingKey(followerId);
        
        // 检查是否为空结果缓存
        if (hasKey(redisKey + ":empty")) {
            return Collections.emptyList();
        }
        
        // 获取关注列表
        Set<Object> followingIds = getSetMembers(redisKey);
        
        if (!followingIds.isEmpty()) {
            return followingIds.stream()
                    .map(this::convertToLong)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }

    /**
     * 缓存用户的粉丝列表
     * 
     * @param followedId  被关注者ID
     * @param followerIds 粉丝用户ID列表
     */
    public void cacheFollowersList(Long followedId, List<Long> followerIds) {
        String redisKey = RedisKeyManager.followFollowersKey(followedId);
        
        deleteCache(redisKey); // 先清空旧数据
        
        if (followerIds != null && !followerIds.isEmpty()) {
            addToSet(redisKey, followerIds.toArray(), RedisKeyManager.RELATION_TTL);
            log.debug("[缓存] 缓存粉丝列表成功 - key: {}, size: {}", redisKey, followerIds.size());
        } else {
            // 缓存空结果，防止缓存穿透
            setValue(redisKey + ":empty", "1", RedisKeyManager.EMPTY_RESULT_TTL);
        }
    }

    /**
     * 从缓存获取用户的粉丝列表
     * 
     * @param followedId 被关注者ID
     * @return 粉丝用户ID列表
     */
    public List<Long> getFollowersListFromCache(Long followedId) {
        String redisKey = RedisKeyManager.followFollowersKey(followedId);
        
        // 检查是否为空结果缓存
        if (hasKey(redisKey + ":empty")) {
            return Collections.emptyList();
        }
        
        // 获取粉丝列表
        Set<Object> followerIds = getSetMembers(redisKey);
        
        if (!followerIds.isEmpty()) {
            return followerIds.stream()
                    .map(this::convertToLong)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }

    /**
     * 缓存用户关注数
     * 
     * @param userId 用户ID
     * @param count  关注数
     */
    public void cacheFollowingCount(Long userId, int count) {
        String redisKey = RedisKeyManager.followFollowingCountKey(userId);
        setValue(redisKey, count, RedisKeyManager.RELATION_TTL);
    }

    /**
     * 从缓存获取用户关注数
     * 
     * @param userId 用户ID
     * @return 关注数
     */
    public Integer getFollowingCountFromCache(Long userId) {
        String redisKey = RedisKeyManager.followFollowingCountKey(userId);
        Object value = getValue(redisKey);
        return convertToInteger(value);
    }

    /**
     * 缓存用户粉丝数
     * 
     * @param userId 用户ID
     * @param count  粉丝数
     */
    public void cacheFollowersCount(Long userId, int count) {
        String redisKey = RedisKeyManager.followFollowersCountKey(userId);
        setValue(redisKey, count, RedisKeyManager.RELATION_TTL);
    }

    /**
     * 从缓存获取用户粉丝数
     * 
     * @param userId 用户ID
     * @return 粉丝数
     */
    public Integer getFollowersCountFromCache(Long userId) {
        String redisKey = RedisKeyManager.followFollowersCountKey(userId);
        Object value = getValue(redisKey);
        return convertToInteger(value);
    }

    /**
     * 缓存互相关注列表
     * 
     * @param userId          用户ID
     * @param mutualFollowIds 互相关注的用户ID列表
     */
    public void cacheMutualFollows(Long userId, List<Long> mutualFollowIds) {
        String redisKey = RedisKeyManager.followMutualKey(userId);
        
        deleteCache(redisKey); // 先清空旧数据
        
        if (mutualFollowIds != null && !mutualFollowIds.isEmpty()) {
            addToSet(redisKey, mutualFollowIds.toArray(), RedisKeyManager.RELATION_TTL);
            log.debug("[缓存] 缓存互相关注列表成功 - key: {}, size: {}", redisKey, mutualFollowIds.size());
        } else {
            // 缓存空结果，防止缓存穿透
            setValue(redisKey + ":empty", "1", RedisKeyManager.EMPTY_RESULT_TTL);
        }
    }

    /**
     * 从缓存获取互相关注列表
     * 
     * @param userId 用户ID
     * @return 互相关注的用户ID列表
     */
    public List<Long> getMutualFollowsFromCache(Long userId) {
        String redisKey = RedisKeyManager.followMutualKey(userId);
        
        // 检查是否为空结果缓存
        if (hasKey(redisKey + ":empty")) {
            return Collections.emptyList();
        }
        
        // 获取互相关注列表
        Set<Object> mutualFollowIds = getSetMembers(redisKey);
        
        if (!mutualFollowIds.isEmpty()) {
            return mutualFollowIds.stream()
                    .map(this::convertToLong)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }

    /**
     * 缓存关注状态
     * 
     * @param followerId  关注者ID
     * @param followedId  被关注者ID
     * @param isFollowing 是否已关注
     */
    public void cacheFollowStatus(Long followerId, Long followedId, boolean isFollowing) {
        String redisKey = RedisKeyManager.followStatusKey(followerId, followedId);
        setValue(redisKey, isFollowing ? "1" : "0", RedisKeyManager.RELATION_TTL);
    }

    /**
     * 从缓存获取关注状态
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 是否已关注
     */
    public Boolean getFollowStatusFromCache(Long followerId, Long followedId) {
        String redisKey = RedisKeyManager.followStatusKey(followerId, followedId);
        Object value = getValue(redisKey);
        return value != null ? "1".equals(convertToString(value)) : null;
    }

    /**
     * 删除用户的关注相关缓存
     * 
     * @param userId 用户ID
     */
    public void removeUserFollowCache(Long userId) {
        // 批量删除所有相关缓存
        java.util.List<String> keys = java.util.Arrays.asList(
            RedisKeyManager.followFollowingKey(userId),
            RedisKeyManager.followFollowingKey(userId) + ":empty",
            RedisKeyManager.followFollowersKey(userId),
            RedisKeyManager.followFollowersKey(userId) + ":empty",
            RedisKeyManager.followFollowingCountKey(userId),
            RedisKeyManager.followFollowersCountKey(userId),
            RedisKeyManager.followMutualKey(userId),
            RedisKeyManager.followMutualKey(userId) + ":empty"
        );
        deleteCacheBatch(keys);
        log.debug("[缓存] 删除用户关注相关缓存成功 - userId: {}", userId);
    }

    /**
     * 删除关注关系相关缓存
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    public void removeFollowRelationCache(Long followerId, Long followedId) {
        // 删除关注状态缓存
        String statusKey = RedisKeyManager.followStatusKey(followerId, followedId);
        deleteCache(statusKey);
        
        // 删除关注者和被关注者的相关缓存
        removeUserFollowCache(followerId);
        removeUserFollowCache(followedId);
        
        log.debug("[缓存] 删除关注关系相关缓存成功 - followerId: {}, followedId: {}", followerId, followedId);
    }
}
