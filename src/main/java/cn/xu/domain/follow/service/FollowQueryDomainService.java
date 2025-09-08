package cn.xu.domain.follow.service;

import cn.xu.domain.follow.model.aggregate.FollowAggregate;
import cn.xu.domain.follow.repository.IFollowAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 关注查询领域服务
 * 专门处理关注相关的查询逻辑，遵循DDD原则
 */
@Service
@RequiredArgsConstructor
public class FollowQueryDomainService {
    
    private static final Logger log = LoggerFactory.getLogger(FollowQueryDomainService.class);
    
    private final IFollowAggregateRepository followAggregateRepository;
    private final FollowCacheDomainService followCacheDomainService;

    /**
     * 获取用户的关注列表（带缓存）
     * 
     * @param followerId 关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系聚合根列表
     */
    public List<FollowAggregate> getFollowingList(Long followerId, Integer pageNo, Integer pageSize) {
        try {
            log.info("[关注查询] 开始获取关注列表 - 关注者: {}, 页码: {}, 页面大小: {}", 
                    followerId, pageNo, pageSize);
            
            // 先从缓存获取
            // 注意：关注列表通常变化频繁，缓存命中率可能不高，这里可以根据实际需求决定是否使用缓存
            
            // 从数据库获取
            List<FollowAggregate> followingList = followAggregateRepository
                    .findFollowingList(followerId, pageNo, pageSize);
            
            // 异步更新缓存
            followCacheDomainService.cacheFollowingList(followerId, followingList);
            
            log.info("[关注查询] 获取关注列表成功 - 关注者: {}, 返回数量: {}", followerId, followingList != null ? followingList.size() : 0);
            return followingList != null ? followingList : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注查询] 获取关注列表失败 - 关注者: {}, 页码: {}, 页面大小: {}", 
                     followerId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户的粉丝列表（带缓存）
     * 
     * @param followedId 被关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系聚合根列表
     */
    public List<FollowAggregate> getFollowersList(Long followedId, Integer pageNo, Integer pageSize) {
        try {
            log.info("[关注查询] 开始获取粉丝列表 - 被关注者: {}, 页码: {}, 页面大小: {}", 
                    followedId, pageNo, pageSize);
            
            // 从数据库获取
            List<FollowAggregate> followersList = followAggregateRepository
                    .findFollowersList(followedId, pageNo, pageSize);
            
            // 异步更新缓存
            followCacheDomainService.cacheFollowersList(followedId, followersList);
            
            log.info("[关注查询] 获取粉丝列表成功 - 被关注者: {}, 返回数量: {}", followedId, followersList != null ? followersList.size() : 0);
            return followersList != null ? followersList : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注查询] 获取粉丝列表失败 - 被关注者: {}, 页码: {}, 页面大小: {}", 
                     followedId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户关注数（带缓存）
     * 
     * @param followerId 关注者ID
     * @return 关注数
     */
    public int getFollowingCount(Long followerId) {
        try {
            log.info("[关注查询] 开始获取关注数 - 关注者: {}", followerId);
            
            // 先从缓存获取
            Integer cachedCount = followCacheDomainService.getFollowingCountFromCache(followerId);
            if (cachedCount != null) {
                log.info("[关注查询] 从缓存获取关注数成功 - 关注者: {}, 数量: {}", followerId, cachedCount);
                return cachedCount;
            }
            
            // 从数据库获取
            int count = followAggregateRepository.countFollowing(followerId);
            
            // 异步更新缓存
            followCacheDomainService.cacheFollowingCount(followerId, count);
            
            log.info("[关注查询] 获取关注数成功 - 关注者: {}, 数量: {}", followerId, count);
            return count;
        } catch (Exception e) {
            log.error("[关注查询] 获取关注数失败 - 关注者: {}", followerId, e);
            return 0;
        }
    }

    /**
     * 获取用户粉丝数（带缓存）
     * 
     * @param followedId 被关注者ID
     * @return 粉丝数
     */
    public int getFollowersCount(Long followedId) {
        try {
            log.info("[关注查询] 开始获取粉丝数 - 被关注者: {}", followedId);
            
            // 先从缓存获取
            Integer cachedCount = followCacheDomainService.getFollowersCountFromCache(followedId);
            if (cachedCount != null) {
                log.info("[关注查询] 从缓存获取粉丝数成功 - 被关注者: {}, 数量: {}", followedId, cachedCount);
                return cachedCount;
            }
            
            // 从数据库获取
            int count = followAggregateRepository.countFollowers(followedId);
            
            // 异步更新缓存
            followCacheDomainService.cacheFollowersCount(followedId, count);
            
            log.info("[关注查询] 获取粉丝数成功 - 被关注者: {}, 数量: {}", followedId, count);
            return count;
        } catch (Exception e) {
            log.error("[关注查询] 获取粉丝数失败 - 被关注者: {}", followedId, e);
            return 0;
        }
    }

    /**
     * 获取互相关注列表（带缓存）
     * 
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 互相关注的用户ID列表
     */
    public List<Long> getMutualFollows(Long userId, Integer pageNo, Integer pageSize) {
        try {
            log.info("[关注查询] 开始获取互相关注列表 - 用户: {}, 页码: {}, 页面大小: {}", 
                    userId, pageNo, pageSize);
            
            // 先从缓存获取
            List<Long> cachedMutualFollows = followCacheDomainService.getMutualFollowsFromCache(userId);
            if (cachedMutualFollows != null && !cachedMutualFollows.isEmpty()) {
                log.info("[关注查询] 从缓存获取互相关注列表成功 - 用户: {}, 数量: {}", userId, cachedMutualFollows.size());
                return cachedMutualFollows;
            }
            
            // 从数据库获取
            List<Long> mutualFollows = followAggregateRepository.findMutualFollows(userId, pageNo, pageSize);
            
            // 异步更新缓存
            followCacheDomainService.cacheMutualFollows(userId, mutualFollows);
            
            log.info("[关注查询] 获取互相关注列表成功 - 用户: {}, 返回数量: {}", userId, mutualFollows != null ? mutualFollows.size() : 0);
            return mutualFollows != null ? mutualFollows : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注查询] 获取互相关注列表失败 - 用户: {}, 页码: {}, 页面大小: {}", 
                     userId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取关注状态（带缓存）
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 是否已关注
     */
    public boolean isFollowing(Long followerId, Long followedId) {
        try {
            log.info("[关注查询] 开始获取关注状态 - 关注者: {}, 被关注者: {}", followerId, followedId);
            
            // 先从缓存获取
            Boolean cachedStatus = followCacheDomainService.getFollowStatusFromCache(followerId, followedId);
            if (cachedStatus != null) {
                log.info("[关注查询] 从缓存获取关注状态成功 - 关注者: {}, 被关注者: {}, 状态: {}", 
                        followerId, followedId, cachedStatus);
                return cachedStatus;
            }
            
            // 从数据库获取
            boolean isFollowing = followAggregateRepository
                    .findByFollowerAndFollowed(followerId, followedId)
                    .map(FollowAggregate::isFollowed)
                    .orElse(false);
            
            // 异步更新缓存
            followCacheDomainService.cacheFollowStatus(followerId, followedId, isFollowing);
            
            log.info("[关注查询] 获取关注状态成功 - 关注者: {}, 被关注者: {}, 状态: {}", 
                    followerId, followedId, isFollowing);
            return isFollowing;
        } catch (Exception e) {
            log.error("[关注查询] 获取关注状态失败 - 关注者: {}, 被关注者: {}", followerId, followedId, e);
            return false;
        }
    }
}