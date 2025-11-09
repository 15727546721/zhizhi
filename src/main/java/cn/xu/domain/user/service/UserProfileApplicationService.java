package cn.xu.domain.user.service;

import cn.xu.application.service.FavoriteApplicationService;
import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.domain.post.repository.IPostTopicRepository;
import cn.xu.domain.post.service.IPostService;
import cn.xu.domain.user.model.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 个人主页应用服务
 * 协调多个领域服务，聚合个人主页所需的所有数据
 * 符合DDD规范，属于应用层服务
 * 
 * 核心业务职责：
 * 1. 聚合用户基本信息
 * 2. 聚合用户统计数据
 * 3. 判断是否为当前用户自己的主页
 * 4. 判断是否已关注（查看他人主页时）
 * 
 * @author zhizhi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileApplicationService {
    
    private final IUserService userService;
    private final IPostService postService;
    private final IFollowService followService;
    private final FavoriteApplicationService favoriteApplicationService;
    private final ICommentRepository commentRepository;
    private final IPostTopicRepository postTopicRepository;
    
    /**
     * 获取个人主页数据
     * 
     * @param userId 用户ID（目标用户）
     * @param currentUserId 当前登录用户ID（可为null，表示未登录）
     * @return 个人主页数据聚合对象
     */
    public UserProfileData getUserProfile(Long userId, Long currentUserId) {
        log.info("[个人主页应用服务] 开始获取个人主页数据 - userId: {}, currentUserId: {}", userId, currentUserId);
        
        try {
            // 1. 获取用户基本信息（只查询一次）
            UserEntity user = userService.getUserInfo(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "用户不存在");
            }
            
            // 1.1 检查用户状态（非自己的主页时，检查用户是否被封禁）
            if (currentUserId == null || !currentUserId.equals(userId)) {
                // 查看他人主页时，如果用户被封禁，不允许查看
                if (user.getStatus() == UserEntity.UserStatus.BANNED) {
                    throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "该用户已被封禁");
                }
            }
            
            // 2. 判断是否为当前用户自己的主页
            boolean isOwnProfile = currentUserId != null && currentUserId.equals(userId);
            
            // 3. 获取统计数据（传入已查询的user实体，避免重复查询）
            UserProfileStats stats = getUserProfileStats(userId, user);
            
            // 4. 判断关注关系（查看他人主页时）
            Boolean isFollowing = null;  // 当前用户是否关注了目标用户
            Boolean isFollowedBy = null; // 目标用户是否关注了当前用户
            if (!isOwnProfile && currentUserId != null) {
                isFollowing = followService.isFollowing(currentUserId, userId);
                isFollowedBy = followService.isFollowing(userId, currentUserId);
            }
            
            // 5. 构建返回对象
            UserProfileData profileData = UserProfileData.builder()
                    .user(user)
                    .stats(stats)
                    .isOwnProfile(isOwnProfile)
                    .isFollowing(isFollowing)
                    .isFollowedBy(isFollowedBy)
                    .build();
            
            log.info("[个人主页应用服务] 获取个人主页数据成功 - userId: {}", userId);
            return profileData;
            
        } catch (BusinessException e) {
            // 业务异常直接抛出
            log.warn("[个人主页应用服务] 获取个人主页数据失败 - userId: {}, error: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[个人主页应用服务] 获取个人主页数据失败 - userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "获取个人主页数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取个人主页统计数据
     * 
     * @param userId 用户ID
     * @param user 用户实体（已查询，避免重复查询）
     * @return 统计数据
     */
    private UserProfileStats getUserProfileStats(Long userId, UserEntity user) {
        // 获取帖子数（已发布的）
        long postCount = postService.countPublishedByUserId(userId);
        
        // 获取关注数
        int followCount = followService.getFollowingCount(userId);
        
        // 获取粉丝数
        int fansCount = followService.getFollowersCount(userId);
        
        // 获取获赞数（从传入的用户实体中获取，避免重复查询）
        long likeCount = user != null && user.getLikeCount() != null ? user.getLikeCount() : 0L;
        
        // 获取收藏数（从收藏应用服务获取）
        long collectionCount = 0L;
        try {
            collectionCount = favoriteApplicationService.getFavoriteCount(userId);
        } catch (Exception e) {
            log.warn("[个人主页应用服务] 获取用户收藏数失败，用户ID: {}", userId, e);
        }
        
        // 获取评论数（从评论仓储获取）
        long commentCount = 0L;
        try {
            Long commentCountLong = commentRepository.countByUserId(userId);
            commentCount = commentCountLong != null ? commentCountLong : 0L;
        } catch (Exception e) {
            log.warn("[个人主页应用服务] 获取用户评论数失败，用户ID: {}", userId, e);
        }
        
        // 获取话题数（从帖子话题关联表统计）
        long topicCount = 0L;
        try {
            Long topicCountLong = postTopicRepository.countTopicsByUserId(userId);
            topicCount = topicCountLong != null ? topicCountLong : 0L;
        } catch (Exception e) {
            log.warn("[个人主页应用服务] 获取用户话题数失败，用户ID: {}", userId, e);
        }
        
        return UserProfileStats.builder()
                .postCount(postCount)
                .followCount((long) followCount)
                .fansCount((long) fansCount)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .collectionCount(collectionCount)
                .topicCount(topicCount)
                .build();
    }
    
    /**
     * 个人主页数据聚合对象
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UserProfileData {
        private UserEntity user;
        private UserProfileStats stats;
        private boolean isOwnProfile;
        private Boolean isFollowing;  // 当前用户是否关注了目标用户
        private Boolean isFollowedBy; // 目标用户是否关注了当前用户
    }
    
    /**
     * 个人主页统计数据
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UserProfileStats {
        private Long postCount;
        private Long followCount;
        private Long fansCount;
        private Long likeCount;
        private Long commentCount;
        private Long collectionCount;
        private Long topicCount;
    }
}

