package cn.xu.repository;

import cn.xu.model.entity.Follow;

import java.util.List;
import java.util.Optional;

/**
 * 关注关系仓储接口
 */
public interface FollowRepository {

    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    void save(Follow follow);

    void updateStatus(Long followerId, Long followedId, Integer status);

    List<Follow> findFollowingList(Long followerId, int offset, int size);

    List<Follow> findFollowersList(Long followedId, int offset, int size);

    Long countFollowing(Long followerId);

    Long countFollowers(Long followedId);

    Integer findStatus(Long followerId, Long followedId);
}
