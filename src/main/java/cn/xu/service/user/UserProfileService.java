package cn.xu.service.user;

import cn.xu.common.ResponseCode;
import cn.xu.model.entity.User;
import cn.xu.repository.CommentRepository;
import cn.xu.service.favorite.FavoriteService;
import cn.xu.service.follow.FollowService;
import cn.xu.service.post.PostService;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户资料服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserService userService;
    private final PostService postService;
    private final FollowService followService;
    private final FavoriteService favoriteService;
    private final CommentRepository commentRepository;

    /**
     * 获取用户个人资料
     *
     * @param userId 用户ID，必须是有效的
     * @param currentUserId 当前登录用户ID，若为null，表示未登录
     * @return 用户个人资料数据
     */
    public UserProfileData getUserProfile(Long userId, Long currentUserId) {
        log.info("[用户个人资料服务] 获取用户个人资料 - userId: {}, currentUserId: {}", userId, currentUserId);

        try {
            // 1. 获取用户基本信息
            User user = userService.getUserInfo(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "用户不存在");
            }

            // 1.1 判断是否允许用户访问该功能
            if (currentUserId == null || !currentUserId.equals(userId)) {
                // 判断用户状态，若被禁用，拒绝访问
                if (user.getStatus() != null && user.getStatus() == User.STATUS_DISABLED) {
                    throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户已被禁用");
                }
            }

            // 2. 判断是否为当前登录用户
            boolean isOwnProfile = currentUserId != null && currentUserId.equals(userId);

            // 3. 获取用户统计数据
            UserProfileStats stats = getUserProfileStats(userId, user);

            // 4. 判断当前用户是否关注此用户
            Boolean isFollowing = null;  // 当前用户是否关注该用户
            Boolean isFollowedBy = null; // 该用户是否关注当前用户
            if (!isOwnProfile && currentUserId != null) {
                isFollowing = followService.isFollowed(currentUserId, userId);
                isFollowedBy = followService.isFollowed(userId, currentUserId);
            }

            // 5. 返回用户资料数据
            UserProfileData profileData = UserProfileData.builder()
                    .user(user)
                    .stats(stats)
                    .isOwnProfile(isOwnProfile)
                    .isFollowing(isFollowing)
                    .isFollowedBy(isFollowedBy)
                    .build();

            log.info("[用户个人资料服务] 获取用户个人资料成功 - userId: {}", userId);
            return profileData;

        } catch (BusinessException e) {
            // 业务异常捕获并返回
            log.warn("[用户个人资料服务] 获取用户个人资料失败 - userId: {}, error: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[用户个人资料服务] 获取用户个人资料失败 - userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "获取用户个人资料失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户统计数据
     */
    private UserProfileStats getUserProfileStats(Long userId, User user) {
        long postCount = postService.countPublishedByUserId(userId);
        long followCount = user.getFollowCount() != null ? user.getFollowCount() : 0;
        long fansCount = user.getFansCount() != null ? user.getFansCount() : 0;
        long likeCount = user.getLikeCount() != null ? user.getLikeCount() : 0;
        long commentCount = commentRepository.countByUserId(userId);
        long collectionCount = favoriteService.countByUserId(userId);
        
        return UserProfileStats.builder()
                .postCount(postCount)
                .followCount(followCount)
                .fansCount(fansCount)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .collectionCount(collectionCount)
                .build();
    }

    /**
     * 用户个人资料数据
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserProfileData {
        private User user;
        private UserProfileStats stats;
        private Boolean isOwnProfile;
        private Boolean isFollowing;
        private Boolean isFollowedBy;
    }

    /**
     * 用户统计数据
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserProfileStats {
        private Long postCount;
        private Long followCount;
        private Long fansCount;
        private Long likeCount;
        private Long commentCount;
        private Long collectionCount;
    }
}