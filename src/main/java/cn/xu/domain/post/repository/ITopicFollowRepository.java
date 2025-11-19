package cn.xu.domain.post.repository;

import cn.xu.infrastructure.persistent.po.TopicFollow;
import java.util.List;

/**
 * 话题关注仓储接口
 */
public interface ITopicFollowRepository {
    void save(TopicFollow topicFollow);
    void remove(Long userId, Long topicId);
    TopicFollow findByUserIdAndTopicId(Long userId, Long topicId);
    List<TopicFollow> findByUserId(Long userId);
    Long countByTopicId(Long topicId);
}
