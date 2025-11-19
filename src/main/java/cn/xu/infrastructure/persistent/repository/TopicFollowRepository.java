package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.post.repository.ITopicFollowRepository;
import cn.xu.infrastructure.persistent.dao.TopicFollowMapper;
import cn.xu.infrastructure.persistent.po.TopicFollow;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class TopicFollowRepository implements ITopicFollowRepository {

    @Resource
    private TopicFollowMapper topicFollowMapper;

    @Override
    public void save(TopicFollow topicFollow) {
        topicFollowMapper.insert(topicFollow);
    }

    @Override
    public void remove(Long userId, Long topicId) {
        topicFollowMapper.deleteByUserIdAndTopicId(userId, topicId);
    }

    @Override
    public TopicFollow findByUserIdAndTopicId(Long userId, Long topicId) {
        return topicFollowMapper.selectByUserIdAndTopicId(userId, topicId);
    }

    @Override
    public List<TopicFollow> findByUserId(Long userId) {
        return topicFollowMapper.selectByUserId(userId);
    }

    @Override
    public Long countByTopicId(Long topicId) {
        return topicFollowMapper.selectCountByTopicId(topicId);
    }
}
