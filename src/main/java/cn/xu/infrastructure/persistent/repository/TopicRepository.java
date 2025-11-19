package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.entity.TopicEntity;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.domain.post.repository.IPostTopicRepository;
import cn.xu.domain.post.repository.ITopicRepository;
import cn.xu.infrastructure.persistent.dao.TopicMapper;
import cn.xu.infrastructure.persistent.po.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
    private final IPostTopicRepository postTopicRepository;
    private final IPostRepository postRepository;
    
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
    
    @Override
    public List<TopicEntity> getTopicsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Topic> topics = topicMapper.selectByIds(ids);
        return topics.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Long> findTopicIdsByPostId(Long postId) {
        if (postId == null) {
            return new ArrayList<>();
        }
        // 通过PostTopicRepository获取话题ID列表
        return postTopicRepository.getTopicIdsByPostId(postId);
    }
    
    @Override
    public List<PostEntity> findPostsByTopicId(Long topicId, int offset, int limit) {
        if (topicId == null) {
            return new ArrayList<>();
        }
        // 1. 通过PostTopicRepository获取帖子ID列表
        List<Long> postIds = postTopicRepository.getPostIdsByTopicId(topicId, offset, limit);
        
        if (postIds == null || postIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 通过PostRepository获取帖子实体列表
        return postRepository.findPostsByIds(postIds);
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
