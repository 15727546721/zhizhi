package cn.xu.domain.follow.repository;

import cn.xu.domain.follow.model.entity.UserFollowEntity;

import java.util.List;

public interface IFollowRepository {
    /**
     * 保存关注关系
     */
    void save(UserFollowEntity entity);

    /**
     * 更新关注状态
     */
    void updateStatus(Long followerId, Long followedId, Integer status);

    /**
     * 获取关注关系
     */
    UserFollowEntity getByFollowerAndFollowed(Long followerId, Long followedId);

    /**
     * 获取用户的关注列表
     */
    List<UserFollowEntity> listByFollowerId(Long followerId);

    /**
     * 获取用户的粉丝列表
     */
    List<UserFollowEntity> listByFollowedId(Long followedId);

    /**
     * 统计用户关注数
     */
    int countFollowing(Long followerId);

    /**
     * 统计用户粉丝数
     */
    int countFollowers(Long followedId);

    /**
     * 查询关注状态
     *
     * @param followerId
     * @param followedId
     * @return
     */
    Integer findStatus(long followerId, long followedId);
}