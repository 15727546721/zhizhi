package cn.xu.infrastructure.persistent.repository;

import cn.xu.api.web.model.dto.essay.TopicQueryRequest;
import cn.xu.domain.essay.model.entity.TopicEntity;
import cn.xu.domain.essay.repository.ITopicRepository;
import cn.xu.infrastructure.persistent.dao.TopicMapper;
import cn.xu.infrastructure.persistent.po.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 随笔话题仓储实现类
 * 负责随笔话题数据的访问和操作
 */
@Slf4j
@Repository("essayTopicRepository")
@RequiredArgsConstructor
public class EssayTopicRepository implements ITopicRepository {

    private final TopicMapper topicMapper;
    
    @Override
    public Long save(TopicEntity topicEntity) {
        // 实现保存话题逻辑
        Topic topic = convertToPO(topicEntity);
        // 这里需要实现具体的保存逻辑，可能需要修改TopicMapper
        return topic.getId();
    }
    
    @Override
    public void update(TopicEntity topicEntity) {
        // 实现更新话题逻辑
        Topic topic = convertToPO(topicEntity);
        // 这里需要实现具体的更新逻辑，可能需要修改TopicMapper
    }
    
    @Override
    public void deleteById(Long id) {
        // 实现删除话题逻辑
        // 这里需要实现具体的删除逻辑，可能需要修改TopicMapper
    }
    
    @Override
    public TopicEntity findById(Long id) {
        Topic topic = topicMapper.getTopicById(id);
        if (topic == null) {
            return null;
        }
        return convertToEntity(topic);
    }
    
    @Override
    public List<TopicEntity> findAll() {
        List<Topic> topics = topicMapper.getAllTopics();
        return topics.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public TopicEntity findByName(String name) {
        Topic topic = topicMapper.findByName(name);
        if (topic == null) {
            return null;
        }
        return convertToEntity(topic);
    }
    
    @Override
    public List<TopicEntity> getPageByName(TopicQueryRequest request) {
        // 实现分页查询逻辑
        // 这里需要实现具体的分页查询逻辑，可能需要修改TopicMapper
        return findAll();
    }
    
    /**
     * 将领域实体转换为持久化对象
     */
    private Topic convertToPO(TopicEntity topicEntity) {
        if (topicEntity == null) {
            return null;
        }
        
        return Topic.builder()
                .id(topicEntity.getId())
                .name(topicEntity.getName())
                .createTime(topicEntity.getCreateTime())
                .updateTime(topicEntity.getUpdateTime())
                .build();
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
                .createTime(topic.getCreateTime())
                .updateTime(topic.getUpdateTime())
                .build();
    }
}