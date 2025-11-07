package cn.xu.domain.recommend.service.impl;

import cn.xu.domain.follow.model.entity.FollowRelationEntity;
import cn.xu.domain.follow.repository.IFollowRepository;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.domain.recommend.service.IRecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 简化版推荐服务实现
 * 适合小系统：不依赖用户行为数据，使用热门+精选+关注的简单策略
 * 
 * 业务合理性：
 * 1. 小系统数据稀疏，用户行为推荐效果差
 * 2. 用户更习惯主动浏览（最新、热门、关注）
 * 3. 简单推荐策略已经足够，投入产出比更高
 * 
 * 推荐策略：
 * - 匿名用户（未登录）：没有行为数据，返回 50% 热门帖子 + 50% 精选帖子
 * - 新用户（已登录但无关注、无点赞、无收藏）：没有行为数据，返回 50% 热门帖子 + 50% 精选帖子
 * - 有行为数据的用户（已登录且有关注）：返回 30% 关注用户帖子 + 40% 热门帖子 + 30% 精选帖子
 */
@Slf4j
@Service("simpleRecommendService")
@RequiredArgsConstructor
public class SimpleRecommendServiceImpl implements IRecommendService {
    
    private final IPostRepository postRepository;
    private final IFollowRepository followRepository;
    
    @Override
    public List<PostEntity> getRecommendedPosts(Long userId, Integer pageNo, Integer pageSize, Long categoryId) {
        log.info("获取简化推荐帖子，用户ID: {}, 页码: {}, 每页数量: {}, 分类ID: {}", userId, pageNo, pageSize, categoryId);
        
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        List<PostEntity> recommendations = new ArrayList<>();
        
        // 如果指定了分类，直接返回该分类的帖子（按时间倒序）
        if (categoryId != null) {
            List<PostEntity> categoryPosts = postRepository.findByCategoryId(categoryId, offset, pageSize);
            log.info("按分类推荐完成，推荐数量: {}, 分类ID: {}", categoryPosts.size(), categoryId);
            return categoryPosts;
        }
        
        // 检查用户是否有关注的人（这是判断用户是否有行为数据的关键指标）
        // 新用户和匿名用户都没有行为数据，应该统一处理
        boolean hasFollowing = false;
        Set<Long> followingUserIds = null;
        
        if (userId != null) {
            try {
                List<FollowRelationEntity> followingList = followRepository.listByFollowerId(userId);
                if (followingList != null && !followingList.isEmpty()) {
                    followingUserIds = followingList.stream()
                            .map(FollowRelationEntity::getFollowedId)
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toSet());
                    hasFollowing = !followingUserIds.isEmpty();
                }
            } catch (Exception e) {
                log.warn("获取用户关注列表失败，用户ID: {}", userId, e);
            }
        }
        
        // 如果用户有关注的人，说明有行为数据，使用混合推荐策略
        if (hasFollowing && followingUserIds != null) {
            // 策略：30% 关注用户帖子 + 40% 热门帖子 + 30% 精选帖子
            int followingCount = (int) (pageSize * 0.3);
            int hotCount = (int) (pageSize * 0.4);
            int featuredCount = pageSize - followingCount - hotCount;
            
            // 1. 获取关注用户的帖子（30%）
            try {
                List<PostEntity> followingPosts = postRepository.findByUserIds(
                        new ArrayList<>(followingUserIds), offset, followingCount);
                recommendations.addAll(followingPosts);
                log.debug("获取关注用户帖子: {}", followingPosts.size());
            } catch (Exception e) {
                log.warn("获取关注用户帖子失败", e);
            }
            
            // 2. 获取热门帖子（40%）
            List<PostEntity> hotPosts = postRepository.findHotPosts(offset, hotCount);
            recommendations.addAll(hotPosts);
            
            // 3. 获取精选帖子（30%）
            List<PostEntity> featuredPosts = postRepository.findFeaturedPosts(offset, featuredCount);
            recommendations.addAll(featuredPosts);
        } else {
            // 策略：50% 热门帖子 + 50% 精选帖子
            // 适用于：匿名用户（未登录）和新用户（已登录但无行为数据）
            int hotCount = pageSize / 2;
            int featuredCount = pageSize - hotCount;
            
            // 1. 获取热门帖子（50%）
            List<PostEntity> hotPosts = postRepository.findHotPosts(offset, hotCount);
            recommendations.addAll(hotPosts);
            
            // 2. 获取精选帖子（50%）
            List<PostEntity> featuredPosts = postRepository.findFeaturedPosts(offset, featuredCount);
            recommendations.addAll(featuredPosts);
        }
        
        // 如果推荐数量不足，用热门帖子补充
        if (recommendations.size() < pageSize) {
            int remaining = pageSize - recommendations.size();
            List<PostEntity> additionalHotPosts = postRepository.findHotPosts(
                    recommendations.size(), remaining);
            recommendations.addAll(additionalHotPosts);
        }
        
        // 限制返回数量
        if (recommendations.size() > pageSize) {
            recommendations = recommendations.subList(0, pageSize);
        }
        
        log.info("简化推荐完成，推荐数量: {}, 策略: {}", 
                recommendations.size(), hasFollowing ? "关注+热门+精选" : "热门+精选");
        return recommendations;
    }
    
    @Override
    public List<PostEntity> getCollaborativeFilteringRecommendations(Long userId, int limit) {
        // 小系统不实现协同过滤，返回空列表
        log.debug("小系统不实现协同过滤推荐");
        return new ArrayList<>();
    }
    
    @Override
    public List<PostEntity> getContentBasedRecommendations(Long userId, int limit) {
        // 小系统不实现基于内容的推荐，返回空列表
        log.debug("小系统不实现基于内容的推荐");
        return new ArrayList<>();
    }
    
    @Override
    public List<PostEntity> getFollowingBasedRecommendations(Long userId, int limit) {
        // 如果需要，可以实现基于关注的推荐
        // 但建议在getRecommendedPosts中直接处理，不需要单独方法
        log.debug("小系统不单独实现基于关注的推荐");
        return new ArrayList<>();
    }
    
    @Override
    public List<PostEntity> getHotRecommendations(int limit) {
        return postRepository.findHotPosts(0, limit);
    }
}

