package cn.xu.domain.recommend.service.impl;

import cn.xu.domain.favorite.repository.IFavoriteRepository;
import cn.xu.domain.follow.repository.IFollowRepository;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeAggregateRepository;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.domain.recommend.service.IRecommendService;
import cn.xu.infrastructure.cache.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现类
 * 实现基于用户行为的内容推荐算法
 */
/**
 * 完整版推荐服务实现（适合大系统）
 * 
 * 注意：当前默认使用简化版推荐服务（SimpleRecommendServiceImpl）
 * 如需使用完整版，请在PostService中切换注入的Bean
 */
@Slf4j
@Service("fullRecommendService")  // 使用不同的Bean名称，避免冲突
@RequiredArgsConstructor
public class RecommendServiceImpl implements IRecommendService {
    
    private final IPostRepository postRepository;
    private final IFavoriteRepository favoriteRepository;
    private final ILikeAggregateRepository likeAggregateRepository;
    private final IFollowRepository followRepository;
    private final cn.xu.domain.post.repository.IPostTagRepository postTagRepository;
    private final cn.xu.domain.post.repository.IPostTopicRepository postTopicRepository;
    private final RedisService redisService;
    
    // 推荐策略权重配置
    private static final double COLLABORATIVE_WEIGHT = 0.4;  // 协同过滤权重
    private static final double CONTENT_WEIGHT = 0.3;        // 内容推荐权重
    private static final double FOLLOWING_WEIGHT = 0.2;      // 关注推荐权重
    private static final double HOT_WEIGHT = 0.1;            // 热度推荐权重
    
    // 缓存过期时间（秒）
    private static final int CACHE_EXPIRE_SECONDS = 3600; // 1小时
    
    @Override
    public List<PostEntity> getRecommendedPosts(Long userId, Integer pageNo, Integer pageSize, Long categoryId) {
        log.info("开始获取推荐帖子，用户ID: {}, 页码: {}, 每页数量: {}, 分类ID: {}", userId, pageNo, pageSize, categoryId);
        
        // 如果指定了分类，直接返回该分类的帖子（按时间倒序）
        if (categoryId != null) {
            int offset = Math.max(0, (pageNo - 1) * pageSize);
            List<PostEntity> categoryPosts = postRepository.findByCategoryId(categoryId, offset, pageSize);
            log.info("按分类推荐完成，推荐数量: {}, 分类ID: {}", categoryPosts.size(), categoryId);
            return categoryPosts;
        }
        
        // 如果用户未登录，直接返回热门推荐
        if (userId == null) {
            return getHotRecommendations(pageSize);
        }
        
        // 检查缓存（缓存key包含categoryId，如果为null则不包含）
        String cacheKey = "recommend:posts:" + userId + ":" + pageNo + ":" + pageSize;
        List<PostEntity> cachedResult = getCachedRecommendations(cacheKey);
        if (cachedResult != null) {
            log.debug("从缓存获取推荐结果，用户ID: {}", userId);
            return cachedResult;
        }
        
        // 混合推荐策略
        List<PostEntity> recommendations = new ArrayList<>();
        
        // 1. 基于用户行为的协同过滤推荐（40%）
        int collaborativeCount = (int) (pageSize * COLLABORATIVE_WEIGHT);
        List<PostEntity> collaborativePosts = getCollaborativeFilteringRecommendations(userId, collaborativeCount);
        recommendations.addAll(collaborativePosts);
        
        // 2. 基于内容的推荐（30%）
        int contentCount = (int) (pageSize * CONTENT_WEIGHT);
        List<PostEntity> contentPosts = getContentBasedRecommendations(userId, contentCount);
        // 去重
        final Set<Long> existingPostIds1 = recommendations.stream()
                .map(PostEntity::getId)
                .collect(Collectors.toSet());
        contentPosts.stream()
                .filter(post -> !existingPostIds1.contains(post.getId()))
                .forEach(recommendations::add);
        
        // 3. 基于关注用户的推荐（20%）
        int followingCount = (int) (pageSize * FOLLOWING_WEIGHT);
        List<PostEntity> followingPosts = getFollowingBasedRecommendations(userId, followingCount);
        final Set<Long> existingPostIds2 = recommendations.stream()
                .map(PostEntity::getId)
                .collect(Collectors.toSet());
        followingPosts.stream()
                .filter(post -> !existingPostIds2.contains(post.getId()))
                .forEach(recommendations::add);
        
        // 4. 如果推荐数量不足，用热门帖子补充（10%）
        if (recommendations.size() < pageSize) {
            int hotCount = pageSize - recommendations.size();
            List<PostEntity> hotPosts = getHotRecommendations(hotCount);
            final Set<Long> existingPostIds3 = recommendations.stream()
                    .map(PostEntity::getId)
                    .collect(Collectors.toSet());
            final int finalHotCount = hotCount;  // 确保hotCount是final
            hotPosts.stream()
                    .filter(post -> !existingPostIds3.contains(post.getId()))
                    .limit(finalHotCount)
                    .forEach(recommendations::add);
        }
        
        // 限制返回数量
        List<PostEntity> result = recommendations.stream()
                .limit(pageSize)
                .collect(Collectors.toList());
        
        // 缓存结果
        cacheRecommendations(cacheKey, result);
        
        log.info("推荐完成，用户ID: {}, 推荐数量: {}", userId, result.size());
        return result;
    }
    
    @Override
    public List<PostEntity> getCollaborativeFilteringRecommendations(Long userId, int limit) {
        log.debug("开始协同过滤推荐，用户ID: {}, 推荐数量: {}", userId, limit);
        
        try {
            // 1. 获取用户的行为数据（点赞和收藏的帖子）
            List<Long> userLikedPosts = getUserLikedPosts(userId);
            List<Long> userFavoritedPosts = getUserFavoritedPosts(userId);
            
            // 如果用户没有任何行为数据，返回空列表
            if (userLikedPosts.isEmpty() && userFavoritedPosts.isEmpty()) {
                log.debug("用户没有行为数据，无法进行协同过滤推荐");
                return Collections.emptyList();
            }
            
            // 2. 找到与当前用户行为相似的用户
            Set<Long> userInteractedPosts = new HashSet<>();
            userInteractedPosts.addAll(userLikedPosts);
            userInteractedPosts.addAll(userFavoritedPosts);
            
            // 3. 找到也喜欢这些帖子的其他用户
            Map<Long, Integer> similarUsers = new HashMap<>();
            for (Long postId : userInteractedPosts) {
                // 获取点赞该帖子的用户
                List<Long> likedUsers = getPostLikedUsers(postId);
                for (Long similarUserId : likedUsers) {
                    if (!similarUserId.equals(userId)) {
                        similarUsers.put(similarUserId, similarUsers.getOrDefault(similarUserId, 0) + 1);
                    }
                }
                
                // 获取收藏该帖子的用户
                List<Long> favoritedUsers = getPostFavoritedUsers(postId);
                for (Long similarUserId : favoritedUsers) {
                    if (!similarUserId.equals(userId)) {
                        similarUsers.put(similarUserId, similarUsers.getOrDefault(similarUserId, 0) + 1);
                    }
                }
            }
            
            // 4. 找到相似用户喜欢的帖子（排除用户已经交互过的）
            Map<Long, Integer> candidatePosts = new HashMap<>();
            for (Map.Entry<Long, Integer> entry : similarUsers.entrySet()) {
                Long similarUserId = entry.getKey();
                Integer similarity = entry.getValue();
                
                // 获取相似用户喜欢的帖子
                List<Long> similarUserLikedPosts = getUserLikedPosts(similarUserId);
                List<Long> similarUserFavoritedPosts = getUserFavoritedPosts(similarUserId);
                
                // 计算推荐分数（相似度 * 权重）
                for (Long postId : similarUserLikedPosts) {
                    if (!userInteractedPosts.contains(postId)) {
                        candidatePosts.put(postId, candidatePosts.getOrDefault(postId, 0) + similarity * 2);
                    }
                }
                
                for (Long postId : similarUserFavoritedPosts) {
                    if (!userInteractedPosts.contains(postId)) {
                        candidatePosts.put(postId, candidatePosts.getOrDefault(postId, 0) + similarity * 3);
                    }
                }
            }
            
            // 5. 按分数排序，取前limit个
            List<Long> recommendedPostIds = candidatePosts.entrySet().stream()
                    .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            if (recommendedPostIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 6. 获取帖子实体
            List<PostEntity> recommendedPosts = postRepository.findPostsByIds(recommendedPostIds);
            
            // 保持排序
            Map<Long, PostEntity> postMap = recommendedPosts.stream()
                    .collect(Collectors.toMap(PostEntity::getId, post -> post));
            
            return recommendedPostIds.stream()
                    .map(postMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("协同过滤推荐失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<PostEntity> getContentBasedRecommendations(Long userId, int limit) {
        log.debug("开始基于内容的推荐，用户ID: {}, 推荐数量: {}", userId, limit);
        
        try {
            // 1. 获取用户喜欢的标签和话题
            List<Long> userLikedPosts = getUserLikedPosts(userId);
            List<Long> userFavoritedPosts = getUserFavoritedPosts(userId);
            
            Set<Long> userInteractedPosts = new HashSet<>();
            userInteractedPosts.addAll(userLikedPosts);
            userInteractedPosts.addAll(userFavoritedPosts);
            
            // 2. 获取用户喜欢的帖子关联的标签和话题
            Set<Long> preferredTagIds = new HashSet<>();
            Set<Long> preferredTopicIds = new HashSet<>();
            
            for (Long postId : userInteractedPosts) {
                // 获取帖子的标签
                List<Long> tagIds = postTagRepository.getTagIdsByPostId(postId);
                preferredTagIds.addAll(tagIds);
                
                // 获取帖子的话题
                List<Long> topicIds = postTopicRepository.getTopicIdsByPostId(postId);
                preferredTopicIds.addAll(topicIds);
            }
            
            // 如果用户没有喜欢的标签和话题，返回空列表
            if (preferredTagIds.isEmpty() && preferredTopicIds.isEmpty()) {
                log.debug("用户没有喜欢的标签和话题，无法进行基于内容的推荐");
                return Collections.emptyList();
            }
            
            // 3. 找到包含这些标签或话题的帖子（排除用户已交互的）
            Map<Long, Integer> candidatePosts = new HashMap<>();
            
            // 基于标签推荐
            for (Long tagId : preferredTagIds) {
                List<PostEntity> posts = postRepository.findByTagId(tagId, 0, 50);
                for (PostEntity post : posts) {
                    if (!userInteractedPosts.contains(post.getId())) {
                        candidatePosts.put(post.getId(), candidatePosts.getOrDefault(post.getId(), 0) + 2);
                    }
                }
            }
            
            // 基于话题推荐
            for (Long topicId : preferredTopicIds) {
                List<Long> postIds = postTopicRepository.getPostIdsByTopicId(topicId, 0, 50);
                List<PostEntity> posts = postRepository.findPostsByIds(postIds);
                for (PostEntity post : posts) {
                    if (!userInteractedPosts.contains(post.getId())) {
                        candidatePosts.put(post.getId(), candidatePosts.getOrDefault(post.getId(), 0) + 3);
                    }
                }
            }
            
            // 4. 按分数排序，取前limit个
            List<Long> recommendedPostIds = candidatePosts.entrySet().stream()
                    .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            if (recommendedPostIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 5. 获取帖子实体
            List<PostEntity> recommendedPosts = postRepository.findPostsByIds(recommendedPostIds);
            
            // 保持排序
            Map<Long, PostEntity> postMap = recommendedPosts.stream()
                    .collect(Collectors.toMap(PostEntity::getId, post -> post));
            
            return recommendedPostIds.stream()
                    .map(postMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("基于内容的推荐失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<PostEntity> getFollowingBasedRecommendations(Long userId, int limit) {
        log.debug("开始基于关注用户的推荐，用户ID: {}, 推荐数量: {}", userId, limit);
        
        try {
            // 1. 获取用户关注的人
            List<cn.xu.domain.follow.model.entity.FollowRelationEntity> followRelations = 
                    followRepository.listByFollowerId(userId);
            
            if (followRelations.isEmpty()) {
                log.debug("用户没有关注任何人，无法进行基于关注的推荐");
                return Collections.emptyList();
            }
            
            // 2. 获取关注用户的ID列表
            List<Long> followingUserIds = followRelations.stream()
                    .map(cn.xu.domain.follow.model.entity.FollowRelationEntity::getFollowedId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            // 3. 获取这些用户发布的帖子（按时间倒序）
            List<PostEntity> followingPosts = postRepository.findByUserIds(followingUserIds, 0, limit * 2);
            
            // 4. 过滤掉用户已经交互过的帖子
            List<Long> userLikedPosts = getUserLikedPosts(userId);
            List<Long> userFavoritedPosts = getUserFavoritedPosts(userId);
            Set<Long> userInteractedPosts = new HashSet<>();
            userInteractedPosts.addAll(userLikedPosts);
            userInteractedPosts.addAll(userFavoritedPosts);
            
            return followingPosts.stream()
                    .filter(post -> !userInteractedPosts.contains(post.getId()))
                    .limit(limit)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("基于关注用户的推荐失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<PostEntity> getHotRecommendations(int limit) {
        log.debug("开始获取热门推荐，推荐数量: {}", limit);
        return postRepository.findHotPosts(0, limit);
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 获取用户点赞的帖子ID列表
     */
    private List<Long> getUserLikedPosts(Long userId) {
        try {
            return likeAggregateRepository.findLikedTargetIdsByUser(userId, LikeType.POST);
        } catch (Exception e) {
            log.warn("获取用户点赞帖子失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取用户收藏的帖子ID列表
     */
    private List<Long> getUserFavoritedPosts(Long userId) {
        try {
            return favoriteRepository.findFavoritedTargetIdsByUserId(userId, "post");
        } catch (Exception e) {
            log.warn("获取用户收藏帖子失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取点赞某个帖子的用户ID列表
     */
    private List<Long> getPostLikedUsers(Long postId) {
        try {
            return likeAggregateRepository.findUserIdsByTarget(postId, LikeType.POST);
        } catch (Exception e) {
            log.warn("获取帖子点赞用户失败，帖子ID: {}", postId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取收藏某个帖子的用户ID列表
     */
    private List<Long> getPostFavoritedUsers(Long postId) {
        try {
            return favoriteRepository.findUserIdsByTarget(postId, "post");
        } catch (Exception e) {
            log.warn("获取帖子收藏用户失败，帖子ID: {}", postId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 从缓存获取推荐结果
     * 小系统优化：使用Redis缓存推荐结果，减少数据库查询
     */
    @SuppressWarnings("unchecked")
    private List<PostEntity> getCachedRecommendations(String cacheKey) {
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null && cached instanceof List) {
                log.debug("从缓存获取推荐结果，key: {}", cacheKey);
                return (List<PostEntity>) cached;
            }
            return null;
        } catch (Exception e) {
            log.warn("从缓存获取推荐结果失败", e);
            return null;
        }
    }
    
    /**
     * 缓存推荐结果
     * 小系统优化：缓存30分钟，平衡实时性和性能
     */
    private void cacheRecommendations(String cacheKey, List<PostEntity> posts) {
        try {
            // 小系统建议：缓存30分钟-1小时
            // 如果用户量较大，可以缩短到10-15分钟
            redisService.set(cacheKey, posts, 1800); // 30分钟
            log.debug("缓存推荐结果成功，key: {}, count: {}", cacheKey, posts.size());
        } catch (Exception e) {
            log.warn("缓存推荐结果失败", e);
        }
    }
}

