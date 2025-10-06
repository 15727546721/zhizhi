package cn.xu.domain.post.service.impl;

import cn.xu.domain.post.model.entity.TopicEntity;
import cn.xu.domain.post.repository.ITopicRepository;
import cn.xu.domain.post.service.ITopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 话题服务实现类
 * 负责话题相关的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements ITopicService {
    
    private final ITopicRepository topicRepository;
    
    @Override
    public void addTopic(String name) {
        topicRepository.addTopic(name);
    }
    
    @Override
    public TopicEntity getTopicById(Long id) {
        return topicRepository.getTopicById(id);
    }
    
    @Override
    public List<TopicEntity> getAllTopics() {
        return topicRepository.getAllTopics();
    }
    
    @Override
    public List<TopicEntity> searchTopics(String keyword) {
        return topicRepository.searchTopics(keyword);
    }
    
    @Override
    public List<TopicEntity> getHotTopics(int limit) {
        return topicRepository.getHotTopics(limit);
    }
}