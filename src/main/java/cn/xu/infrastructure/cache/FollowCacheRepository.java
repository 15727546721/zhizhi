package cn.xu.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 关注缓存仓储
 * 专门处理关注相关的缓存操作，遵循DDD原则
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class FollowCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final int DEFAULT_CACHE_TTL = 1800; // 30分钟
    private static final int EMPTY_RESULT_TTL = 60;    // 1分钟
    
    // Redis Key 前缀
    private static final String FOLLOWING_KEY_PREFIX = "follow:following:";
    private static final String FOLLOWERS_KEY_PREFIX = "follow:followers:";
    private static final String FOLLOWING_COUNT_KEY_PREFIX = "follow:following_count:";
    private static final String FOLLOWERS_COUNT_KEY_PREFIX = "follow:followers_count:";
    private static final String MUTUAL_FOLLOW_KEY_PREFIX = "follow:mutual:";
    private static final String FOLLOW_STATUS_KEY_PREFIX = "follow:status:";

    /**
     * 缓存用户的关注列表
     * 
     * @param followerId 关注者ID
     * @param followingIds 关注的用户ID列表
     */
    public void cacheFollowingList(Long followerId, List<Long> followingIds) {
        String redisKey = FOLLOWING_KEY_PREFIX + followerId;
        
        try {
            // 先清空旧数据
            redisTemplate.delete(redisKey);
            
            if (followingIds != null && !followingIds.isEmpty()) {
                // 添加新的关注列表
                redisTemplate.opsForSet().add(redisKey, followingIds.toArray());
                
                // 设置过期时间
                redisTemplate.expire(redisKey, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            } else {
                // 缓存空结果，防止缓存穿透
                redisTemplate.opsForValue().set(redisKey + ":empty", "1", EMPTY_RESULT_TTL, TimeUnit.SECONDS);
            }
            
            log.debug("缓存关注列表成功: key={}, size={}", redisKey, followingIds != null ? followingIds.size() : 0);
        } catch (Exception e) {
            log.error("缓存关注列表失败: key={}", redisKey, e);
        }
    }

    /**
     * 从缓存获取用户的关注列表
     * 
     * @param followerId 关注者ID
     * @return 关注的用户ID列表
     */
    public List<Long> getFollowingListFromCache(Long followerId) {
        String redisKey = FOLLOWING_KEY_PREFIX + followerId;
        
        try {
            // 检查是否为空结果缓存
            String emptyKey = redisKey + ":empty";
            if (Boolean.TRUE.equals(redisTemplate.hasKey(emptyKey))) {
                return Collections.emptyList();
            }
            
            // 获取关注列表
            Set<Object> followingIds = redisTemplate.opsForSet().members(redisKey);
            
            if (followingIds != null && !followingIds.isEmpty()) {
                return followingIds.stream()
                        .map(this::safeParseUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("从缓存获取关注列表失败: key={}", redisKey, e);
        }
        
        return Collections.emptyList();
    }

    /**
     * 缓存用户的粉丝列表
     * 
     * @param followedId 被关注者ID
     * @param followerIds 粉丝用户ID列表
     */
    public void cacheFollowersList(Long followedId, List<Long> followerIds) {
        String redisKey = FOLLOWERS_KEY_PREFIX + followedId;
        
        try {
            // 先清空旧数据
            redisTemplate.delete(redisKey);
            
            if (followerIds != null && !followerIds.isEmpty()) {
                // 添加新的粉丝列表
                redisTemplate.opsForSet().add(redisKey, followerIds.toArray());
                
                // 设置过期时间
                redisTemplate.expire(redisKey, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            } else {
                // 缓存空结果，防止缓存穿透
                redisTemplate.opsForValue().set(redisKey + ":empty", "1", EMPTY_RESULT_TTL, TimeUnit.SECONDS);
            }
            
            log.debug("缓存粉丝列表成功: key={}, size={}", redisKey, followerIds != null ? followerIds.size() : 0);
        } catch (Exception e) {
            log.error("缓存粉丝列表失败: key={}", redisKey, e);
        }
    }

    /**
     * 从缓存获取用户的粉丝列表
     * 
     * @param followedId 被关注者ID
     * @return 粉丝用户ID列表
     */
    public List<Long> getFollowersListFromCache(Long followedId) {
        String redisKey = FOLLOWERS_KEY_PREFIX + followedId;
        
        try {
            // 检查是否为空结果缓存
            String emptyKey = redisKey + ":empty";
            if (Boolean.TRUE.equals(redisTemplate.hasKey(emptyKey))) {
                return Collections.emptyList();
            }
            
            // 获取粉丝列表
            Set<Object> followerIds = redisTemplate.opsForSet().members(redisKey);
            
            if (followerIds != null && !followerIds.isEmpty()) {
                return followerIds.stream()
                        .map(this::safeParseUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("从缓存获取粉丝列表失败: key={}", redisKey, e);
        }
        
        return Collections.emptyList();
    }

    /**
     * 缓存用户关注数
     * 
     * @param userId 用户ID
     * @param count 关注数
     */
    public void cacheFollowingCount(Long userId, int count) {
        String redisKey = FOLLOWING_COUNT_KEY_PREFIX + userId;
        
        try {
            redisTemplate.opsForValue().set(redisKey, count, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("缓存关注数成功: key={}, count={}", redisKey, count);
        } catch (Exception e) {
            log.error("缓存关注数失败: key={}", redisKey, e);
        }
    }

    /**
     * 从缓存获取用户关注数
     * 
     * @param userId 用户ID
     * @return 关注数
     */
    public Integer getFollowingCountFromCache(Long userId) {
        String redisKey = FOLLOWING_COUNT_KEY_PREFIX + userId;
        
        try {
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
        } catch (Exception e) {
            log.error("从缓存获取关注数失败: key={}", redisKey, e);
        }
        
        return null;
    }

    /**
     * 缓存用户粉丝数
     * 
     * @param userId 用户ID
     * @param count 粉丝数
     */
    public void cacheFollowersCount(Long userId, int count) {
        String redisKey = FOLLOWERS_COUNT_KEY_PREFIX + userId;
        
        try {
            redisTemplate.opsForValue().set(redisKey, count, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("缓存粉丝数成功: key={}, count={}", redisKey, count);
        } catch (Exception e) {
            log.error("缓存粉丝数失败: key={}", redisKey, e);
        }
    }

    /**
     * 从缓存获取用户粉丝数
     * 
     * @param userId 用户ID
     * @return 粉丝数
     */
    public Integer getFollowersCountFromCache(Long userId) {
        String redisKey = FOLLOWERS_COUNT_KEY_PREFIX + userId;
        
        try {
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
        } catch (Exception e) {
            log.error("从缓存获取粉丝数失败: key={}", redisKey, e);
        }
        
        return null;
    }

    /**
     * 缓存互相关注列表
     * 
     * @param userId 用户ID
     * @param mutualFollowIds 互相关注的用户ID列表
     */
    public void cacheMutualFollows(Long userId, List<Long> mutualFollowIds) {
        String redisKey = MUTUAL_FOLLOW_KEY_PREFIX + userId;
        
        try {
            // 先清空旧数据
            redisTemplate.delete(redisKey);
            
            if (mutualFollowIds != null && !mutualFollowIds.isEmpty()) {
                // 添加新的互相关注列表
                redisTemplate.opsForSet().add(redisKey, mutualFollowIds.toArray());
                
                // 设置过期时间
                redisTemplate.expire(redisKey, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            } else {
                // 缓存空结果，防止缓存穿透
                redisTemplate.opsForValue().set(redisKey + ":empty", "1", EMPTY_RESULT_TTL, TimeUnit.SECONDS);
            }
            
            log.debug("缓存互相关注列表成功: key={}, size={}", redisKey, mutualFollowIds != null ? mutualFollowIds.size() : 0);
        } catch (Exception e) {
            log.error("缓存互相关注列表失败: key={}", redisKey, e);
        }
    }

    /**
     * 从缓存获取互相关注列表
     * 
     * @param userId 用户ID
     * @return 互相关注的用户ID列表
     */
    public List<Long> getMutualFollowsFromCache(Long userId) {
        String redisKey = MUTUAL_FOLLOW_KEY_PREFIX + userId;
        
        try {
            // 检查是否为空结果缓存
            String emptyKey = redisKey + ":empty";
            if (Boolean.TRUE.equals(redisTemplate.hasKey(emptyKey))) {
                return Collections.emptyList();
            }
            
            // 获取互相关注列表
            Set<Object> mutualFollowIds = redisTemplate.opsForSet().members(redisKey);
            
            if (mutualFollowIds != null && !mutualFollowIds.isEmpty()) {
                return mutualFollowIds.stream()
                        .map(this::safeParseUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("从缓存获取互相关注列表失败: key={}", redisKey, e);
        }
        
        return Collections.emptyList();
    }

    /**
     * 缓存关注状态
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @param isFollowing 是否已关注
     */
    public void cacheFollowStatus(Long followerId, Long followedId, boolean isFollowing) {
        String redisKey = FOLLOW_STATUS_KEY_PREFIX + followerId + ":" + followedId;
        
        try {
            redisTemplate.opsForValue().set(redisKey, isFollowing ? "1" : "0", DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("缓存关注状态成功: key={}, isFollowing={}", redisKey, isFollowing);
        } catch (Exception e) {
            log.error("缓存关注状态失败: key={}", redisKey, e);
        }
    }

    /**
     * 从缓存获取关注状态
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 是否已关注
     */
    public Boolean getFollowStatusFromCache(Long followerId, Long followedId) {
        String redisKey = FOLLOW_STATUS_KEY_PREFIX + followerId + ":" + followedId;
        
        try {
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value instanceof String) {
                return "1".equals(value);
            }
        } catch (Exception e) {
            log.error("从缓存获取关注状态失败: key={}", redisKey, e);
        }
        
        return null;
    }

    /**
     * 删除用户的关注相关缓存
     * 
     * @param userId 用户ID
     */
    public void removeUserFollowCache(Long userId) {
        try {
            // 删除关注列表缓存
            redisTemplate.delete(FOLLOWING_KEY_PREFIX + userId);
            redisTemplate.delete(FOLLOWING_KEY_PREFIX + userId + ":empty");
            
            // 删除粉丝列表缓存
            redisTemplate.delete(FOLLOWERS_KEY_PREFIX + userId);
            redisTemplate.delete(FOLLOWERS_KEY_PREFIX + userId + ":empty");
            
            // 删除关注数缓存
            redisTemplate.delete(FOLLOWING_COUNT_KEY_PREFIX + userId);
            
            // 删除粉丝数缓存
            redisTemplate.delete(FOLLOWERS_COUNT_KEY_PREFIX + userId);
            
            // 删除互相关注缓存
            redisTemplate.delete(MUTUAL_FOLLOW_KEY_PREFIX + userId);
            redisTemplate.delete(MUTUAL_FOLLOW_KEY_PREFIX + userId + ":empty");
            
            log.debug("删除用户关注相关缓存成功: userId={}", userId);
        } catch (Exception e) {
            log.error("删除用户关注相关缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 删除关注关系相关缓存
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    public void removeFollowRelationCache(Long followerId, Long followedId) {
        try {
            // 删除关注状态缓存
            String statusKey = FOLLOW_STATUS_KEY_PREFIX + followerId + ":" + followedId;
            redisTemplate.delete(statusKey);
            
            // 删除关注者和被关注者的相关缓存
            removeUserFollowCache(followerId);
            removeUserFollowCache(followedId);
            
            log.debug("删除关注关系相关缓存成功: followerId={}, followedId={}", followerId, followedId);
        } catch (Exception e) {
            log.error("删除关注关系相关缓存失败: followerId={}, followedId={}", followerId, followedId, e);
        }
    }

    /**
     * 安全解析用户ID
     * 
     * @param obj 对象
     * @return 用户ID
     */
    private Long safeParseUserId(Object obj) {
        try {
            if (obj instanceof Long) {
                return (Long) obj;
            } else if (obj instanceof String) {
                return Long.parseLong((String) obj);
            } else if (obj instanceof Integer) {
                return ((Integer) obj).longValue();
            }
        } catch (Exception e) {
            log.warn("解析用户ID失败: obj={}", obj, e);
        }
        return null;
    }
}