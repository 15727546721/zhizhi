package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.post.model.entity.TopicEntity;
import cn.xu.domain.post.repository.ITopicRepository;
import cn.xu.infrastructure.persistent.dao.TopicMapper;
import cn.xu.infrastructure.persistent.po.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 话题仓储实现类
 * 负责话题数据的访问和操作
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TopicRepository implements ITopicRepository {

    private final TopicMapper topicMapper;
    
    @Override
    public void addTopic(String name) {
        topicMapper.addTopic(name);
    }
    
    @Override
    public TopicEntity getTopicById(Long id) {
        Topic topic = topicMapper.getTopicById(id);
        if (topic == null) {
            return null;
        }
        return convertToEntity(topic);
    }
    
    @Override
    public List<TopicEntity> getAllTopics() {
        List<Topic> topics = topicMapper.getAllTopics();
        return topics.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TopicEntity> searchTopics(String keyword) {
        List<Topic> topics = topicMapper.searchTopics(keyword);
        return topics.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TopicEntity> getHotTopics(int limit) {
        List<Topic> topics = topicMapper.getHotTopics(limit);
        return topics.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 将持久化对象转换为领域实体
     */
    private TopicEntity convertToEntity(Topic topic) {
        if (topic == null) {
            return null;
        }
        
        return TopicEntity.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .isRecommended(topic.getIsRecommended())
                .usageCount(topic.getUsageCount())
                .createTime(topic.getCreateTime())
                .updateTime(topic.getUpdateTime())
                .build();
    }
}
