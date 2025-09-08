package cn.xu.domain.follow.service;

import cn.xu.domain.follow.model.aggregate.FollowAggregate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 关注应用服务
 * 协调领域服务，处理关注相关的应用层逻辑
 */
@Service
@RequiredArgsConstructor
public class FollowApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(FollowApplicationService.class);
    
    private final FollowManagementDomainService followManagementDomainService;
    private final FollowQueryDomainService followQueryDomainService;
    private final FollowCacheDomainService followCacheDomainService;
    private final FollowRecommendationService followRecommendationService;

    /**
     * 关注用户
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    public void followUser(Long followerId, Long followedId) {
        log.info("[关注应用] 开始关注用户 - 关注者: {}, 被关注者: {}", followerId, followedId);
        
        try {
            // 执行关注操作
            followManagementDomainService.followUser(followerId, followedId);
            
            // 清除相关缓存
            followCacheDomainService.removeFollowRelationCache(followerId, followedId);
            
            log.info("[关注应用] 关注用户成功 - 关注者: {}, 被关注者: {}", followerId, followedId);
        } catch (Exception e) {
            log.error("[关注应用] 关注用户失败 - 关注者: {}, 被关注者: {}", followerId, followedId, e);
            throw e;
        }
    }

    /**
     * 取消关注用户
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    public void unfollowUser(Long followerId, Long followedId) {
        log.info("[关注应用] 开始取消关注用户 - 关注者: {}, 被关注者: {}", followerId, followedId);
        
        try {
            // 执行取消关注操作
            followManagementDomainService.unfollowUser(followerId, followedId);
            
            // 清除相关缓存
            followCacheDomainService.removeFollowRelationCache(followerId, followedId);
            
            log.info("[关注应用] 取消关注用户成功 - 关注者: {}, 被关注者: {}", followerId, followedId);
        } catch (Exception e) {
            log.error("[关注应用] 取消关注用户失败 - 关注者: {}, 被关注者: {}", followerId, followedId, e);
            throw e;
        }
    }

    /**
     * 获取用户的关注列表
     * 
     * @param followerId 关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系聚合根列表
     */
    public List<FollowAggregate> getFollowingList(Long followerId, Integer pageNo, Integer pageSize) {
        log.info("[关注应用] 开始获取关注列表 - 关注者: {}, 页码: {}, 页面大小: {}", 
                followerId, pageNo, pageSize);
        
        try {
            List<FollowAggregate> followingList = followQueryDomainService
                    .getFollowingList(followerId, pageNo, pageSize);
            
            log.info("[关注应用] 获取关注列表成功 - 关注者: {}, 返回数量: {}", followerId, followingList != null ? followingList.size() : 0);
            return followingList != null ? followingList : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注应用] 获取关注列表失败 - 关注者: {}, 页码: {}, 页面大小: {}", 
                     followerId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户的粉丝列表
     * 
     * @param followedId 被关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系聚合根列表
     */
    public List<FollowAggregate> getFollowersList(Long followedId, Integer pageNo, Integer pageSize) {
        log.info("[关注应用] 开始获取粉丝列表 - 被关注者: {}, 页码: {}, 页面大小: {}", 
                followedId, pageNo, pageSize);
        
        try {
            List<FollowAggregate> followersList = followQueryDomainService
                    .getFollowersList(followedId, pageNo, pageSize);
            
            log.info("[关注应用] 获取粉丝列表成功 - 被关注者: {}, 返回数量: {}", followedId, followersList != null ? followersList.size() : 0);
            return followersList != null ? followersList : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注应用] 获取粉丝列表失败 - 被关注者: {}, 页码: {}, 页面大小: {}", 
                     followedId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户关注数
     * 
     * @param followerId 关注者ID
     * @return 关注数
     */
    public int getFollowingCount(Long followerId) {
        log.info("[关注应用] 开始获取关注数 - 关注者: {}", followerId);
        
        try {
            int count = followQueryDomainService.getFollowingCount(followerId);
            
            log.info("[关注应用] 获取关注数成功 - 关注者: {}, 数量: {}", followerId, count);
            return count;
        } catch (Exception e) {
            log.error("[关注应用] 获取关注数失败 - 关注者: {}", followerId, e);
            return 0;
        }
    }

    /**
     * 获取用户粉丝数
     * 
     * @param followedId 被关注者ID
     * @return 粉丝数
     */
    public int getFollowersCount(Long followedId) {
        log.info("[关注应用] 开始获取粉丝数 - 被关注者: {}", followedId);
        
        try {
            int count = followQueryDomainService.getFollowersCount(followedId);
            
            log.info("[关注应用] 获取粉丝数成功 - 被关注者: {}, 数量: {}", followedId, count);
            return count;
        } catch (Exception e) {
            log.error("[关注应用] 获取粉丝数失败 - 被关注者: {}", followedId, e);
            return 0;
        }
    }

    /**
     * 获取互相关注列表
     * 
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 互相关注的用户ID列表
     */
    public List<Long> getMutualFollows(Long userId, Integer pageNo, Integer pageSize) {
        log.info("[关注应用] 开始获取互相关注列表 - 用户: {}, 页码: {}, 页面大小: {}", 
                userId, pageNo, pageSize);
        
        try {
            List<Long> mutualFollows = followQueryDomainService
                    .getMutualFollows(userId, pageNo, pageSize);
            
            log.info("[关注应用] 获取互相关注列表成功 - 用户: {}, 返回数量: {}", userId, mutualFollows != null ? mutualFollows.size() : 0);
            return mutualFollows != null ? mutualFollows : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注应用] 获取互相关注列表失败 - 用户: {}, 页码: {}, 页面大小: {}", 
                     userId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取关注状态
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 是否已关注
     */
    public boolean isFollowing(Long followerId, Long followedId) {
        log.info("[关注应用] 开始获取关注状态 - 关注者: {}, 被关注者: {}", followerId, followedId);
        
        try {
            boolean isFollowing = followQueryDomainService.isFollowing(followerId, followedId);
            
            log.info("[关注应用] 获取关注状态成功 - 关注者: {}, 被关注者: {}, 状态: {}", 
                    followerId, followedId, isFollowing);
            return isFollowing;
        } catch (Exception e) {
            log.error("[关注应用] 获取关注状态失败 - 关注者: {}, 被关注者: {}", followerId, followedId, e);
            return false;
        }
    }

    /**
     * 获取推荐关注的用户
     * 
     * @param userId 当前用户ID
     * @param count 推荐数量
     * @return 推荐的用户ID列表
     */
    public List<Long> getRecommendedUsers(Long userId, int count) {
        log.info("[关注应用] 开始获取推荐用户 - 用户: {}, 数量: {}", userId, count);
        
        try {
            List<Long> recommendedUsers = followRecommendationService.getRecommendedUsers(userId, count);
            
            log.info("[关注应用] 获取推荐用户成功 - 用户: {}, 返回数量: {}", userId, recommendedUsers != null ? recommendedUsers.size() : 0);
            return recommendedUsers != null ? recommendedUsers : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注应用] 获取推荐用户失败 - 用户: {}, 数量: {}", userId, count, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取可能认识的用户
     * 
     * @param userId 当前用户ID
     * @param count 推荐数量
     * @return 可能认识的用户ID列表
     */
    public List<Long> getUsersYouMayKnow(Long userId, int count) {
        log.info("[关注应用] 开始获取可能认识的用户 - 用户: {}, 数量: {}", userId, count);
        
        try {
            List<Long> usersYouMayKnow = followRecommendationService.getUsersYouMayKnow(userId, count);
            
            log.info("[关注应用] 获取可能认识的用户成功 - 用户: {}, 返回数量: {}", userId, usersYouMayKnow != null ? usersYouMayKnow.size() : 0);
            return usersYouMayKnow != null ? usersYouMayKnow : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注应用] 获取可能认识的用户失败 - 用户: {}, 数量: {}", userId, count, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取热门用户推荐
     * 
     * @param userId 当前用户ID
     * @param count 推荐数量
     * @return 热门用户ID列表
     */
    public List<Long> getPopularUsers(Long userId, int count) {
        log.info("[关注应用] 开始获取热门用户推荐 - 用户: {}, 数量: {}", userId, count);
        
        try {
            List<Long> popularUsers = followRecommendationService.getPopularUsers(userId, count);
            
            log.info("[关注应用] 获取热门用户推荐成功 - 用户: {}, 返回数量: {}", userId, popularUsers != null ? popularUsers.size() : 0);
            return popularUsers != null ? popularUsers : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注应用] 获取热门用户推荐失败 - 用户: {}, 数量: {}", userId, count, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取用户的共同关注
     * 
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 共同关注的用户ID列表
     */
    public List<Long> getCommonFollowing(Long userId, Long targetUserId) {
        log.info("[关注应用] 开始获取共同关注 - 用户: {}, 目标用户: {}", userId, targetUserId);
        
        try {
            List<Long> commonFollowing = followRecommendationService.getCommonFollowing(userId, targetUserId);
            
            log.info("[关注应用] 获取共同关注成功 - 用户: {}, 目标用户: {}, 共同关注数量: {}", 
                    userId, targetUserId, commonFollowing != null ? commonFollowing.size() : 0);
            return commonFollowing != null ? commonFollowing : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注应用] 获取共同关注失败 - 用户: {}, 目标用户: {}", userId, targetUserId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取关注用户的粉丝（可能感兴趣的人）
     * 
     * @param userId 当前用户ID
     * @param count 推荐数量
     * @return 可能感兴趣的人的用户ID列表
     */
    public List<Long> getFollowersOfFollowing(Long userId, int count) {
        log.info("[关注应用] 开始获取关注用户的粉丝 - 用户: {}, 数量: {}", userId, count);
        
        try {
            List<Long> followersOfFollowing = followRecommendationService.getFollowersOfFollowing(userId, count);
            
            log.info("[关注应用] 获取关注用户的粉丝成功 - 用户: {}, 返回数量: {}", userId, followersOfFollowing != null ? followersOfFollowing.size() : 0);
            return followersOfFollowing != null ? followersOfFollowing : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注应用] 获取关注用户的粉丝失败 - 用户: {}, 数量: {}", userId, count, e);
            return Collections.emptyList();
        }
    }
}