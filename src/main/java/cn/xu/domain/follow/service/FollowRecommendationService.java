package cn.xu.domain.follow.service;

import cn.xu.domain.follow.model.aggregate.FollowAggregate;
import cn.xu.domain.follow.repository.IFollowAggregateRepository;
import cn.xu.domain.user.repository.IUserAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 关注推荐服务
 * 提供关注相关的推荐功能
 */
@Service
@RequiredArgsConstructor
public class FollowRecommendationService {
    
    private static final Logger log = LoggerFactory.getLogger(FollowRecommendationService.class);
    
    private final IFollowAggregateRepository followAggregateRepository;
    private final IUserAggregateRepository userAggregateRepository;
    
    /**
     * 获取推荐关注的用户
     * 
     * @param userId 当前用户ID
     * @param count 推荐数量
     * @return 推荐的用户ID列表
     */
    public List<Long> getRecommendedUsers(Long userId, int count) {
        try {
            log.info("[关注推荐] 开始获取推荐用户 - 用户: {}, 数量: {}", userId, count);
            
            // 获取用户已关注的用户
            List<Long> followingIds = getFollowingIds(userId);
            
            // 获取用户粉丝
            List<Long> followerIds = getFollowerIds(userId);
            
            // 合并关注和粉丝，作为推荐的基础
            List<Long> relatedUsers = new ArrayList<>();
            relatedUsers.addAll(followingIds);
            relatedUsers.addAll(followerIds);
            
            // 获取这些相关用户关注的用户，作为推荐候选
            List<Long> candidateUsers = new ArrayList<>();
            for (Long relatedUserId : relatedUsers) {
                candidateUsers.addAll(getFollowingIds(relatedUserId));
            }
            
            // 过滤掉自己和已关注的用户
            List<Long> recommendedUsers = candidateUsers.stream()
                    .filter(id -> !id.equals(userId)) // 排除自己
                    .filter(id -> !followingIds.contains(id)) // 排除已关注的用户
                    .distinct() // 去重
                    .limit(count) // 限制数量
                    .collect(Collectors.toList());
            
            // 如果推荐数量不足，随机补充一些活跃用户
            if (recommendedUsers.size() < count) {
                List<Long> randomUsers = getRandomActiveUsers(userId, followingIds, count - recommendedUsers.size());
                recommendedUsers.addAll(randomUsers);
            }
            
            // 再次去重并限制数量
            recommendedUsers = recommendedUsers.stream()
                    .distinct()
                    .limit(count)
                    .collect(Collectors.toList());
            
            log.info("[关注推荐] 获取推荐用户成功 - 用户: {}, 返回数量: {}", userId, recommendedUsers.size());
            return recommendedUsers;
        } catch (Exception e) {
            log.error("[关注推荐] 获取推荐用户失败 - 用户: {}, 数量: {}", userId, count, e);
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
        try {
            log.info("[关注推荐] 开始获取可能认识的用户 - 用户: {}, 数量: {}", userId, count);
            
            // 获取用户的粉丝
            List<Long> followerIds = getFollowerIds(userId);
            
            // 获取粉丝的粉丝（除了自己），这些可能是共同关注的人
            List<Long> candidates = new ArrayList<>();
            for (Long followerId : followerIds) {
                List<Long> followersFollowers = getFollowerIds(followerId);
                candidates.addAll(followersFollowers);
            }
            
            // 过滤掉自己和已关注的用户
            List<Long> followingIds = getFollowingIds(userId);
            
            List<Long> usersYouMayKnow = candidates.stream()
                    .filter(id -> !id.equals(userId)) // 排除自己
                    .filter(id -> !followingIds.contains(id)) // 排除已关注的用户
                    .distinct() // 去重
                    .limit(count) // 限制数量
                    .collect(Collectors.toList());
            
            log.info("[关注推荐] 获取可能认识的用户成功 - 用户: {}, 返回数量: {}", userId, usersYouMayKnow.size());
            return usersYouMayKnow;
        } catch (Exception e) {
            log.error("[关注推荐] 获取可能认识的用户失败 - 用户: {}, 数量: {}", userId, count, e);
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
        try {
            log.info("[关注推荐] 开始获取热门用户推荐 - 用户: {}, 数量: {}", userId, count);
            
            // 获取粉丝数较多的用户作为热门用户
            // 这里简化实现，实际可以结合缓存或更复杂的算法
            
            // 获取用户已关注的用户
            List<Long> followingIds = getFollowingIds(userId);
            
            // 获取一些活跃用户
            List<Long> activeUsers = getRandomActiveUsers(userId, followingIds, count * 2);
            
            // 按粉丝数排序并返回前count个
            List<Long> popularUsers = activeUsers.stream()
                    .sorted((id1, id2) -> {
                        int followers1 = followAggregateRepository.countFollowers(id1);
                        int followers2 = followAggregateRepository.countFollowers(id2);
                        return Integer.compare(followers2, followers1); // 降序排列
                    })
                    .limit(count)
                    .collect(Collectors.toList());
            
            log.info("[关注推荐] 获取热门用户推荐成功 - 用户: {}, 返回数量: {}", userId, popularUsers.size());
            return popularUsers;
        } catch (Exception e) {
            log.error("[关注推荐] 获取热门用户推荐失败 - 用户: {}, 数量: {}", userId, count, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取互相关注的用户
     * 
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 互相关注的用户ID列表
     */
    public List<Long> getMutualFollows(Long userId, Integer pageNo, Integer pageSize) {
        try {
            log.info("[关注推荐] 开始获取互相关注用户 - 用户: {}, 页码: {}, 页面大小: {}", 
                    userId, pageNo, pageSize);
            
            List<Long> mutualFollows = followAggregateRepository.findMutualFollows(userId, pageNo, pageSize);
            
            log.info("[关注推荐] 获取互相关注用户成功 - 用户: {}, 返回数量: {}", userId, mutualFollows != null ? mutualFollows.size() : 0);
            return mutualFollows != null ? mutualFollows : Collections.emptyList();
        } catch (Exception e) {
            log.error("[关注推荐] 获取互相关注用户失败 - 用户: {}, 页码: {}, 页面大小: {}", 
                     userId, pageNo, pageSize, e);
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
        try {
            log.info("[关注推荐] 开始获取共同关注 - 用户: {}, 目标用户: {}", userId, targetUserId);
            
            // 获取两个用户的关注列表
            List<Long> userFollowing = getFollowingIds(userId);
            List<Long> targetFollowing = getFollowingIds(targetUserId);
            
            // 找出共同关注
            List<Long> commonFollowing = userFollowing.stream()
                    .filter(targetFollowing::contains)
                    .collect(Collectors.toList());
            
            log.info("[关注推荐] 获取共同关注成功 - 用户: {}, 目标用户: {}, 共同关注数量: {}", 
                    userId, targetUserId, commonFollowing.size());
            return commonFollowing;
        } catch (Exception e) {
            log.error("[关注推荐] 获取共同关注失败 - 用户: {}, 目标用户: {}", userId, targetUserId, e);
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
        try {
            log.info("[关注推荐] 开始获取关注用户的粉丝 - 用户: {}, 数量: {}", userId, count);
            
            // 获取用户关注的用户
            List<Long> followingIds = getFollowingIds(userId);
            
            // 获取这些用户的所有粉丝
            List<Long> candidates = new ArrayList<>();
            for (Long followingId : followingIds) {
                candidates.addAll(getFollowerIds(followingId));
            }
            
            // 过滤掉自己和已关注的用户
            List<Long> filteredCandidates = candidates.stream()
                    .filter(id -> !id.equals(userId)) // 排除自己
                    .filter(id -> !followingIds.contains(id)) // 排除已关注的用户
                    .distinct() // 去重
                    .limit(count) // 限制数量
                    .collect(Collectors.toList());
            
            log.info("[关注推荐] 获取关注用户的粉丝成功 - 用户: {}, 返回数量: {}", userId, filteredCandidates.size());
            return filteredCandidates;
        } catch (Exception e) {
            log.error("[关注推荐] 获取关注用户的粉丝失败 - 用户: {}, 数量: {}", userId, count, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取用户的关注列表ID
     * 
     * @param userId 用户ID
     * @return 关注的用户ID列表
     */
    private List<Long> getFollowingIds(Long userId) {
        try {
            // 这里简化实现，实际应该分页获取所有关注
            List<FollowAggregate> followingList = followAggregateRepository
                    .findFollowingList(userId, 1, 100); // 假设最多获取100个关注
            
            if (followingList == null) {
                return Collections.emptyList();
            }
            
            return followingList.stream()
                    .map(FollowAggregate::getFollowedId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[关注推荐] 获取用户关注列表失败 - 用户: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取用户的粉丝列表ID
     * 
     * @param userId 用户ID
     * @return 粉丝用户ID列表
     */
    private List<Long> getFollowerIds(Long userId) {
        try {
            // 这里简化实现，实际应该分页获取所有粉丝
            List<FollowAggregate> followersList = followAggregateRepository
                    .findFollowersList(userId, 1, 100); // 假设最多获取100个粉丝
            
            if (followersList == null) {
                return Collections.emptyList();
            }
            
            return followersList.stream()
                    .map(FollowAggregate::getFollowerId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[关注推荐] 获取用户粉丝列表失败 - 用户: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取随机活跃用户
     * 
     * @param userId 当前用户ID
     * @param excludeIds 需要排除的用户ID列表
     * @param count 数量
     * @return 随机活跃用户ID列表
     */
    private List<Long> getRandomActiveUsers(Long userId, List<Long> excludeIds, int count) {
        try {
            // 这里简化实现，实际可以从缓存或数据库中获取活跃用户
            List<Long> activeUsers = new ArrayList<>();
            
            // 获取一些用户作为活跃用户池
            // 这里随机生成一些用户ID作为示例
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < count * 5; i++) { // 生成更多的候选用户
                long randomUserId = random.nextLong(10000, 99999);
                if (!Long.valueOf(randomUserId).equals(userId) && !excludeIds.contains(randomUserId)) {
                    activeUsers.add(randomUserId);
                }
            }
            
            // 随机选择指定数量的用户
            return activeUsers.stream()
                    .limit(count)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[关注推荐] 获取随机活跃用户失败 - 用户: {}, 排除数量: {}, 需要数量: {}", 
                    userId, excludeIds.size(), count, e);
            return Collections.emptyList();
        }
    }
}