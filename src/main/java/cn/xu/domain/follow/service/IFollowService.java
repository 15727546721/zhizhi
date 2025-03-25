package cn.xu.domain.follow.service;

import cn.xu.domain.follow.model.entity.UserFollowEntity;

import java.util.List;

public interface IFollowService {
    /**
     * 关注用户
     */
    void follow(Long followerId, Long followedId);

    /**
     * 取消关注
     */
    void unfollow(Long followerId, Long followedId);

    /**
     * 获取关注状态
     */
    boolean isFollowing(Long followerId, Long followedId);

    /**
     * 获取用户的关注列表
     */
    List<UserFollowEntity> getFollowingList(Long followerId);

    /**
     * 获取用户的粉丝列表
     */
    List<UserFollowEntity> getFollowersList(Long followedId);

    /**
     * 获取用户关注数
     */
    int getFollowingCount(Long followerId);

    /**
     * 获取用户粉丝数
     */
    int getFollowersCount(Long followedId);

    /**
     * 查询用户关注状态
     *
     * @param followerId
     * @param followedId
     * @return
     */
    boolean checkStatus(long followerId, long followedId);
}