package cn.xu.domain.post.service;

import java.util.List;

public interface ITopicFollowService {
    void follow(Long userId, Long topicId);
    void unfollow(Long userId, Long topicId);
    boolean isFollowing(Long userId, Long topicId);
    List<Long> getFollowedTopicIds(Long userId);
    Long getTopicFollowerCount(Long topicId);
}
