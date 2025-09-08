package cn.xu.domain.follow.service;

import cn.xu.domain.follow.model.aggregate.FollowAggregate;
import cn.xu.infrastructure.cache.FollowCacheRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 关注缓存领域服务
 * 专门处理关注相关的缓存逻辑，遵循DDD原则
 */
@Service
@RequiredArgsConstructor
public class FollowCacheDomainService {

    private static final Logger log = LoggerFactory.getLogger(FollowCacheDomainService.class);

    private final FollowCacheRepository followCacheRepository;
    
    // 使用线程池异步更新缓存
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 从缓存获取用户的关注列表
     * 
     * @param followerId 关注者ID
     * @return 关注的用户ID列表
     */
    public List<Long> getFollowingListFromCache(Long followerId) {
        try {
            List<Long> followingIds = followCacheRepository.getFollowingListFromCache(followerId);
            log.debug("从缓存获取关注列表成功 - followerId: {}, count: {}", followerId, followingIds != null ? followingIds.size() : 0);
            return followingIds != null ? followingIds : Collections.emptyList();
        } catch (Exception e) {
            log.error("从缓存获取关注列表失败 - followerId: {}", followerId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 缓存用户的关注列表
     * 
     * @param followerId 关注者ID
     * @param followingList 关注关系聚合根列表
     */
    public void cacheFollowingList(Long followerId, List<FollowAggregate> followingList) {
        try {
            List<Long> followingIds = followingList != null ? followingList.stream()
                    .map(FollowAggregate::getFollowedId)
                    .collect(Collectors.toList()) : Collections.emptyList();
            
            // 异步更新缓存
            CompletableFuture.runAsync(() -> {
                followCacheRepository.cacheFollowingList(followerId, followingIds);
            }, executorService);
            
            log.debug("异步缓存关注列表 - followerId: {}, count: {}", followerId, followingIds.size());
        } catch (Exception e) {
            log.error("缓存关注列表失败 - followerId: {}", followerId, e);
        }
    }

    /**
     * 从缓存获取用户的粉丝列表
     * 
     * @param followedId 被关注者ID
     * @return 粉丝用户ID列表
     */
    public List<Long> getFollowersListFromCache(Long followedId) {
        try {
            List<Long> followerIds = followCacheRepository.getFollowersListFromCache(followedId);
            log.debug("从缓存获取粉丝列表成功 - followedId: {}, count: {}", followedId, followerIds != null ? followerIds.size() : 0);
            return followerIds != null ? followerIds : Collections.emptyList();
        } catch (Exception e) {
            log.error("从缓存获取粉丝列表失败 - followedId: {}", followedId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 缓存用户的粉丝列表
     * 
     * @param followedId 被关注者ID
     * @param followersList 粉丝关系聚合根列表
     */
    public void cacheFollowersList(Long followedId, List<FollowAggregate> followersList) {
        try {
            List<Long> followerIds = followersList != null ? followersList.stream()
                    .map(FollowAggregate::getFollowerId)
                    .collect(Collectors.toList()) : Collections.emptyList();
            
            // 异步更新缓存
            CompletableFuture.runAsync(() -> {
                followCacheRepository.cacheFollowersList(followedId, followerIds);
            }, executorService);
            
            log.debug("异步缓存粉丝列表 - followedId: {}, count: {}", followedId, followerIds.size());
        } catch (Exception e) {
            log.error("缓存粉丝列表失败 - followedId: {}", followedId, e);
        }
    }

    /**
     * 从缓存获取用户关注数
     * 
     * @param userId 用户ID
     * @return 关注数
     */
    public Integer getFollowingCountFromCache(Long userId) {
        try {
            Integer count = followCacheRepository.getFollowingCountFromCache(userId);
            log.debug("从缓存获取关注数成功 - userId: {}, count: {}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("从缓存获取关注数失败 - userId: {}", userId, e);
            return null;
        }
    }

    /**
     * 缓存用户关注数
     * 
     * @param userId 用户ID
     * @param count 关注数
     */
    public void cacheFollowingCount(Long userId, int count) {
        try {
            // 异步更新缓存
            CompletableFuture.runAsync(() -> {
                followCacheRepository.cacheFollowingCount(userId, count);
            }, executorService);
            
            log.debug("异步缓存关注数 - userId: {}, count: {}", userId, count);
        } catch (Exception e) {
            log.error("缓存关注数失败 - userId: {}", userId, e);
        }
    }

    /**
     * 从缓存获取用户粉丝数
     * 
     * @param userId 用户ID
     * @return 粉丝数
     */
    public Integer getFollowersCountFromCache(Long userId) {
        try {
            Integer count = followCacheRepository.getFollowersCountFromCache(userId);
            log.debug("从缓存获取粉丝数成功 - userId: {}, count: {}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("从缓存获取粉丝数失败 - userId: {}", userId, e);
            return null;
        }
    }

    /**
     * 缓存用户粉丝数
     * 
     * @param userId 用户ID
     * @param count 粉丝数
     */
    public void cacheFollowersCount(Long userId, int count) {
        try {
            // 异步更新缓存
            CompletableFuture.runAsync(() -> {
                followCacheRepository.cacheFollowersCount(userId, count);
            }, executorService);
            
            log.debug("异步缓存粉丝数 - userId: {}, count: {}", userId, count);
        } catch (Exception e) {
            log.error("缓存粉丝数失败 - userId: {}", userId, e);
        }
    }

    /**
     * 从缓存获取互相关注列表
     * 
     * @param userId 用户ID
     * @return 互相关注的用户ID列表
     */
    public List<Long> getMutualFollowsFromCache(Long userId) {
        try {
            List<Long> mutualFollowIds = followCacheRepository.getMutualFollowsFromCache(userId);
            log.debug("从缓存获取互相关注列表成功 - userId: {}, count: {}", userId, mutualFollowIds != null ? mutualFollowIds.size() : 0);
            return mutualFollowIds != null ? mutualFollowIds : Collections.emptyList();
        } catch (Exception e) {
            log.error("从缓存获取互相关注列表失败 - userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 缓存互相关注列表
     * 
     * @param userId 用户ID
     * @param mutualFollowIds 互相关注的用户ID列表
     */
    public void cacheMutualFollows(Long userId, List<Long> mutualFollowIds) {
        try {
            // 异步更新缓存
            CompletableFuture.runAsync(() -> {
                followCacheRepository.cacheMutualFollows(userId, mutualFollowIds != null ? mutualFollowIds : Collections.emptyList());
            }, executorService);
            
            log.debug("异步缓存互相关注列表 - userId: {}, count: {}", userId, mutualFollowIds != null ? mutualFollowIds.size() : 0);
        } catch (Exception e) {
            log.error("缓存互相关注列表失败 - userId: {}", userId, e);
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
        try {
            Boolean isFollowing = followCacheRepository.getFollowStatusFromCache(followerId, followedId);
            log.debug("从缓存获取关注状态成功 - followerId: {}, followedId: {}, isFollowing: {}", 
                     followerId, followedId, isFollowing);
            return isFollowing;
        } catch (Exception e) {
            log.error("从缓存获取关注状态失败 - followerId: {}, followedId: {}", followerId, followedId, e);
            return null;
        }
    }

    /**
     * 缓存关注状态
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @param isFollowing 是否已关注
     */
    public void cacheFollowStatus(Long followerId, Long followedId, boolean isFollowing) {
        try {
            // 异步更新缓存
            CompletableFuture.runAsync(() -> {
                followCacheRepository.cacheFollowStatus(followerId, followedId, isFollowing);
            }, executorService);
            
            log.debug("异步缓存关注状态 - followerId: {}, followedId: {}, isFollowing: {}", 
                     followerId, followedId, isFollowing);
        } catch (Exception e) {
            log.error("缓存关注状态失败 - followerId: {}, followedId: {}", followerId, followedId, e);
        }
    }

    /**
     * 删除用户的关注相关缓存
     * 
     * @param userId 用户ID
     */
    public void removeUserFollowCache(Long userId) {
        try {
            // 异步删除缓存
            CompletableFuture.runAsync(() -> {
                followCacheRepository.removeUserFollowCache(userId);
            }, executorService);
            
            log.debug("异步删除用户关注相关缓存 - userId: {}", userId);
        } catch (Exception e) {
            log.error("删除用户关注相关缓存失败 - userId: {}", userId, e);
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
            // 异步删除缓存
            CompletableFuture.runAsync(() -> {
                followCacheRepository.removeFollowRelationCache(followerId, followedId);
            }, executorService);
            
            log.debug("异步删除关注关系相关缓存 - followerId: {}, followedId: {}", followerId, followedId);
        } catch (Exception e) {
            log.error("删除关注关系相关缓存失败 - followerId: {}, followedId: {}", followerId, followedId, e);
        }
    }
}