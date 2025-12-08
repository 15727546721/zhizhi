package cn.xu.service.follow;

import cn.xu.cache.FollowCacheRepository;
import cn.xu.event.publisher.SocialEventPublisher;
import cn.xu.model.entity.Follow;
import cn.xu.model.entity.User;
import cn.xu.model.vo.FollowUserVO;
import cn.xu.repository.impl.FollowRepository;
import cn.xu.repository.impl.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 关注服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final FollowCacheRepository followCacheRepository;
    private final UserRepository userRepository;
    private final SocialEventPublisher socialEventPublisher;

    // ==================== 核心功能 ====================

    /**
     * 关注用户
     *
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long followedId) {
        log.info("[关注服务] 执行关注 - followerId: {}, followedId: {}", followerId, followedId);

        // 验证
        Follow.validateFollowRelation(followerId, followedId);

        // 查询现有关注关系
        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId);

        if (existingFollow.isPresent()) {
            Follow follow = existingFollow.get();
            // 如果已经关注，直接返回
            if (follow.isFollowed()) {
                log.info("[关注服务] 已经关注，无需重复操作 - {}", follow.getSimpleInfo());
                return;
            }
            // 检查当前用户是否关注了这个粉丝（互相关注）
            follow.follow();
            followRepository.save(follow);
            log.info("[关注服务] 重新关注成功 - {}", follow.getSimpleInfo());
        } else {
            // 创建新的关注关系
            Follow follow = Follow.createFollow(followerId, followedId);
            followRepository.save(follow);
            log.info("[关注服务] 新建关注成功 - {}", follow.getSimpleInfo());
        }

        // 更新用户统计（事务内）
        userRepository.increaseFollowCount(followerId);
        userRepository.increaseFansCount(followedId);

        // 事务后更新缓存和发布事件
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.debug("[关注服务] 更新缓存 - followerId: {}, followedId: {}", followerId, followedId);
                        followCacheRepository.removeFollowRelationCache(followerId, followedId);
                        followCacheRepository.removeUserFollowCache(followerId);
                        followCacheRepository.removeUserFollowCache(followedId);
                        
                        // 发布关注事件
                        socialEventPublisher.publishFollowed(followerId, followedId);
                        log.debug("[关注服务] 发布关注事件 - followerId: {}, followeeId: {}", followerId, followedId);
                    }
                }
        );
    }

    /**
     * 取消关注
     *
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followerId, Long followedId) {
        log.info("[关注服务] 执行取消关注 - followerId: {}, followedId: {}", followerId, followedId);

        // 验证关注关系
        Follow.validateFollowRelation(followerId, followedId);

        // 查询关注关系
        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId);

        // 幂等处理：如果关系不存在或已取消关注，直接返回
        if (!existingFollow.isPresent() || !existingFollow.get().isFollowed()) {
            log.info("[关注服务] 未关注或已取消，幂等返回 - followerId: {}, followedId: {}", followerId, followedId);
            return;
        }

        // 执行取消关注
        Follow follow = existingFollow.get();
        follow.unfollow();
        followRepository.save(follow);

        // 更新用户统计（事务内）
        userRepository.decreaseFollowCount(followerId);
        userRepository.decreaseFansCount(followedId);

        log.info("[关注服务] 取消关注成功 - {}", follow.getSimpleInfo());

        // 事务后更新缓存和发布事件
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.debug("[关注服务] 更新缓存 - followerId: {}, followedId: {}", followerId, followedId);
                        followCacheRepository.removeFollowRelationCache(followerId, followedId);
                        followCacheRepository.removeUserFollowCache(followerId);
                        followCacheRepository.removeUserFollowCache(followedId);
                        
                        // 发布取消关注事件
                        socialEventPublisher.publishUnfollowed(followerId, followedId);
                        log.debug("[关注服务] 发布取消关注事件 - followerId: {}, followeeId: {}", followerId, followedId);
                    }
                }
        );
    }

    // ==================== 查询功能 ====================

    /**
     * 检查是否关注
     *
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 是否关注
     */
    public boolean isFollowed(Long followerId, Long followedId) {
        if (followerId == null || followedId == null) {
            return false;
        }

        if (followerId.equals(followedId)) {
            return false;
        }

        // 先查缓存
        Boolean cached = followCacheRepository.getFollowStatusFromCache(followerId, followedId);
        if (cached != null) {
            log.debug("[关注服务] 缓存命中 - followerId: {}, followedId: {}, result: {}",
                    followerId, followedId, cached);
            return cached;
        }

        // 查数据库
        boolean followed = followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .map(Follow::isFollowed)
                .orElse(false);

        // 回写缓存（缓存自愈）
        followCacheRepository.cacheFollowStatus(followerId, followedId, followed);
        log.debug("[关注服务] 缓存自愈 - followerId: {}, followedId: {}, result: {}",
                followerId, followedId, followed);

        return followed;
    }

    /**
     * 批量检查关注状态
     *
     * @param followerId 关注者ID
     * @param followedIds 被关注者ID列表
     * @return Map<被关注者ID, 是否关注>
     */
    public Map<Long, Boolean> batchCheckFollowStatus(Long followerId, List<Long> followedIds) {
        if (followerId == null || followedIds == null || followedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        log.debug("[关注服务] 批量检查关注状态 - followerId: {}, count: {}", followerId, followedIds.size());

        return followedIds.stream()
                .collect(Collectors.toMap(
                        followedId -> followedId,
                        followedId -> isFollowed(followerId, followedId)
                ));
    }

    /**
     * 获取用户关注的所有用户ID列表（用于关注动态Feed）
     *
     * @param userId 用户ID
     * @param limit 最大数量限制（避免数据量过大）
     * @return 关注用户ID列表
     */
    public List<Long> getFollowingUserIds(Long userId, int limit) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 获取关注列表（取前limit个）
        List<Follow> followList = followRepository.findFollowingList(userId, 0, limit);

        return followList.stream()
                .map(Follow::getFollowedId)
                .collect(Collectors.toList());
    }

    /**
     * 获取关注列表（我关注的人）
     *
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 关注列表
     */
    public List<Follow> getFollowingList(Long userId, Integer page, Integer size) {
        if (userId == null || page == null || size == null) {
            return Collections.emptyList();
        }

        int offset = (page - 1) * size;
        log.debug("[关注服务] 查询关注列表 - userId: {}, page: {}, size: {}", userId, page, size);

        return followRepository.findFollowingList(userId, offset, size);
    }

    /**
     * 获取粉丝列表（关注我的人）
     *
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 粉丝列表
     */
    public List<Follow> getFollowersList(Long userId, Integer page, Integer size) {
        if (userId == null || page == null || size == null) {
            return Collections.emptyList();
        }

        int offset = (page - 1) * size;
        log.debug("[关注服务] 查询粉丝列表 - userId: {}, page: {}, size: {}", userId, page, size);

        return followRepository.findFollowersList(userId, offset, size);
    }

    // ==================== 统计功能 ====================

    /**
     * 统计关注数
     *
     * @param userId 用户ID
     * @return 关注数量
     */
    public Long countFollowing(Long userId) {
        if (userId == null) {
            return 0L;
        }

        Long count = followRepository.countFollowing(userId);
        log.debug("[关注服务] 统计关注数 - userId: {}, count: {}", userId, count);

        return count;
    }

    /**
     * 统计粉丝数
     *
     * @param userId 用户ID
     * @return 粉丝数量
     */
    public Long countFollowers(Long userId) {
        if (userId == null) {
            return 0L;
        }

        Long count = followRepository.countFollowers(userId);
        log.debug("[关注服务] 统计粉丝数 - userId: {}, count: {}", userId, count);

        return count;
    }

    /**
     * 批量统计粉丝数
     *
     * @param userIds 用户ID列表
     * @return Map<用户ID, 粉丝数>
     */
    public Map<Long, Long> batchCountFollowers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        this::countFollowers
                ));
    }

    // ==================== 前端VO转换方法 ====================

    /**
     * 获取关注列表（带用户信息）
     *
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 关注用户信息列表
     */
    public List<FollowUserVO> getFollowingListWithUserInfo(Long userId, Integer page, Integer size) {
        if (userId == null || page == null || size == null) {
            return Collections.emptyList();
        }

        // 获取关注列表
        List<Follow> followList = getFollowingList(userId, page, size);
        if (followList.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取关注用户ID列表
        List<Long> followedUserIds = followList.stream()
                .map(Follow::getFollowedId)
                .collect(Collectors.toList());

        // 批量查询用户信息
        List<User> users = userRepository.findByIds(followedUserIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 转换为VO
        return followList.stream()
                .map(follow -> {
                    User user = userMap.get(follow.getFollowedId());
                    if (user == null) {
                        return null;
                    }

                    return FollowUserVO.builder()
                            .id(follow.getId())
                            .userId(user.getId())
                            .nickname(user.getNickname())
                            .avatar(user.getAvatar())
                            .description(user.getDescription())
                            .username(user.getUsername())
                            .followTime(follow.getCreateTime())
                            .isFollowing(true) // 关注列表中的都是正在关注的
                            .fansCount(user.getFansCount())
                            .followCount(user.getFollowCount())
                            .build();
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
    }

    /**
     * 获取粉丝列表（带用户信息）
     *
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 粉丝用户信息列表
     */
    public List<FollowUserVO> getFollowersListWithUserInfo(Long userId, Integer page, Integer size) {
        if (userId == null || page == null || size == null) {
            return Collections.emptyList();
        }

        // 获取粉丝列表
        List<Follow> followersList = getFollowersList(userId, page, size);
        if (followersList.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取关注者用户的ID列表
        List<Long> followerUserIds = followersList.stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toList());

        // 批量查询用户信息
        List<User> users = userRepository.findByIds(followerUserIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 当前用户ID（用于检查是否互相关注）
        Long currentUserId = userId;

        // 转换为VO
        return followersList.stream()
                .map(follow -> {
                    User user = userMap.get(follow.getFollowerId());
                    if (user == null) {
                        return null;
                    }

                    // 检查当前用户是否关注了这个粉丝（互相关注）
                    boolean isFollowing = isFollowed(currentUserId, user.getId());

                    return FollowUserVO.builder()
                            .id(follow.getId())
                            .userId(user.getId())
                            .nickname(user.getNickname())
                            .avatar(user.getAvatar())
                            .description(user.getDescription())
                            .username(user.getUsername())
                            .followTime(follow.getCreateTime())
                            .isFollowing(isFollowing)
                            .fansCount(user.getFansCount())
                            .followCount(user.getFollowCount())
                            .build();
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
    }

    /**
     * 搜索关注用户（用于@提及）
     *
     * @param userId 当前用户ID
     * @param keyword 搜索关键词（匹配昵称或用户名）
     * @param limit 返回数量限制
     * @return 匹配的关注用户列表
     */
    public List<FollowUserVO> searchFollowingUsers(Long userId, String keyword, Integer limit) {
        if (userId == null) {
            return Collections.emptyList();
        }
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50; // 限制最大返回数量
        }

        // 获取用户的全部关注ID（限制500个）
        List<Long> followingUserIds = getFollowingUserIds(userId, 500);
        if (followingUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询用户信息
        List<User> users = userRepository.findByIds(followingUserIds);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        // 按关键词过滤（昵称或用户名包含关键词）
        String lowerKeyword = (keyword == null) ? "" : keyword.toLowerCase().trim();
        final int maxLimit = limit;

        return users.stream()
                .filter(user -> {
                    if (lowerKeyword.isEmpty()) {
                        return true; // 无关键词时返回全部
                    }
                    String nickname = user.getNickname() != null ? user.getNickname().toLowerCase() : "";
                    String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
                    return nickname.contains(lowerKeyword) || username.contains(lowerKeyword);
                })
                .limit(maxLimit)
                .map(user -> FollowUserVO.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .username(user.getUsername())
                        .description(user.getDescription())
                        .isFollowing(true)
                        .build())
                .collect(Collectors.toList());
    }
}
