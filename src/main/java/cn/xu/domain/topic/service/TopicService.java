package cn.xu.domain.topic.service;

import cn.xu.domain.topic.command.CreateTopicCommand;
import cn.xu.domain.topic.entity.Topic;
import cn.xu.domain.topic.model.entity.TopicEntity;
import cn.xu.domain.topic.repository.ITopicRepository;
import cn.xu.domain.topic.service.ITopicCategoryService;
import cn.xu.exception.AppException;
import cn.xu.infrastructure.common.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TopicService {

    @Resource
    private ITopicRepository topicRepository;
    
    @Resource
    private ITopicCategoryService topicCategoryService;

    /**
     * 创建话题
     */
    @Transactional(rollbackFor = Exception.class)
    public Topic createTopic(CreateTopicCommand command) {
        // 1. 检查分类是否存在
        if (topicCategoryService.getCategory(command.getCategoryId()) == null) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "话题分类不存在");
        }
        
        // 2. 转换为领域实体
        TopicEntity topicEntity = TopicEntity.builder()
                .userId(command.getUserId())
                .content(command.getContent())
                .images(command.getImages())
                .categoryId(command.getCategoryId())
                .build();
                
        // 3. 验证
        topicEntity.validate();
        
        // 4. 保存
        Long topicId = topicRepository.save(topicEntity);
        
        // 5. 返回完整的话题信息
        TopicEntity savedTopic = topicRepository.findById(topicId);
        return convertToTopic(savedTopic);
    }

    /**
     * 更新话题
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTopic(Topic topic) {
        TopicEntity existingTopic = topicRepository.findById(topic.getId());
        if (existingTopic == null) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "话题不存在");
        }
        
        TopicEntity topicEntity = convertToTopicEntity(topic);
        topicEntity.validate();
        existingTopic.update(topicEntity);
        topicRepository.update(existingTopic);
    }

    /**
     * 获取话题列表
     */
    public List<Topic> getTopicList(Long categoryId) {
        List<TopicEntity> topicEntities = topicRepository.findByCategoryId(categoryId);
        return topicEntities.stream()
                .map(this::convertToTopic)
                .collect(Collectors.toList());
    }

    /**
     * 获取热门话题
     */
    public List<Topic> getHotTopics(int limit) {
        List<TopicEntity> topicEntities = topicRepository.findHotTopics(limit);
        return topicEntities.stream()
                .map(this::convertToTopic)
                .collect(Collectors.toList());
    }

    /**
     * 删除话题
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Long id) {
        TopicEntity existingTopic = topicRepository.findById(id);
        if (existingTopic == null) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "话题不存在");
        }
        topicRepository.deleteById(id);
    }

    /**
     * 批量删除话题
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopics(List<Long> ids) {
        topicRepository.deleteByIds(ids);
    }

    /**
     * 将Topic转换为TopicEntity
     */
    private TopicEntity convertToTopicEntity(Topic topic) {
        if (topic == null) {
            return null;
        }
        return TopicEntity.builder()
                .id(topic.getId())
                .userId(topic.getUserId())
                .content(topic.getContent())
                .images(topic.getImages())
                .categoryId(topic.getCategoryId())
                .createTime(topic.getCreateTime())
                .updateTime(topic.getUpdateTime())
                .build();
    }

    /**
     * 将TopicEntity转换为Topic
     */
    private Topic convertToTopic(TopicEntity topicEntity) {
        if (topicEntity == null) {
            return null;
        }
        return Topic.builder()
                .id(topicEntity.getId())
                .userId(topicEntity.getUserId())
                .content(topicEntity.getContent())
                .images(topicEntity.getImages())
                .categoryId(topicEntity.getCategoryId())
                .createTime(topicEntity.getCreateTime())
                .updateTime(topicEntity.getUpdateTime())
                .build();
    }

    /**
     * 获取所有话题列表
     */
    public List<Topic> getAllTopics() {
        log.info("获取所有话题列表");
        List<TopicEntity> topicEntities = topicRepository.findAll();
        return topicEntities.stream()
                .map(this::convertToTopic)
                .collect(Collectors.toList());
    }
} 