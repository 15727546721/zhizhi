package cn.xu.domain.topic.service;

import cn.xu.domain.file.service.MinioService;
import cn.xu.domain.topic.command.CreateTopicCommand;
import cn.xu.domain.topic.entity.Topic;
import cn.xu.domain.topic.model.entity.TopicEntity;
import cn.xu.domain.topic.repository.ITopicRepository;
import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TopicService {

    @Resource
    private ITopicRepository topicRepository;

    @Resource
    private ITopicCategoryService topicCategoryService;

    @Resource
    private MinioService minioService;

    /**
     * 创建话题
     */
    @Transactional(rollbackFor = Exception.class)
    public Topic createTopic(CreateTopicCommand command) {
        List<String> formalImages = new ArrayList<>();
        try {
            // 1. 检查分类是否存在
            if (command.getCategoryId() != null && topicCategoryService.getCategory(command.getCategoryId()) == null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "话题分类不存在");
            }

            // 2. 如果有图片，将临时图片移动到永久目录
            if (command.getImages() != null && !command.getImages().isEmpty()) {
                formalImages = minioService.moveToFormal(command.getImages());
            }

            // 3. 转换为领域实体
            TopicEntity topicEntity = TopicEntity.builder()
                    .userId(command.getUserId())
                    .content(command.getContent())
                    .images(formalImages)
                    .categoryId(command.getCategoryId())
                    .build();

            // 4. 验证
            topicEntity.validate();

            // 5. 保存
            Long topicId = topicRepository.save(topicEntity);

            // 6. 返回完整的话题信息
            return convertToTopic(topicRepository.findById(topicId));

        } catch (Exception e) {
            // 发生异常时，删除已移动的永久图片
            if (!formalImages.isEmpty()) {
                minioService.deleteTopicImages(formalImages);
            }
            throw e;
        }
    }

    /**
     * 更新话题
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTopic(TopicEntity topicEntity) {
        TopicEntity existingTopic = topicRepository.findById(topicEntity.getId());
        if (existingTopic == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "话题不存在");
        }

        topicEntity.validate();
        topicRepository.update(topicEntity);
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
        Topic topic = getTopicById(id);
        if (topic != null && topic.getImages() != null && !topic.getImages().isEmpty()) {
            minioService.deleteTopicImages(topic.getImages());
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

    /**
     * 更新话题图片
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTopicImages(Long topicId, List<String> imageUrls) {
        TopicEntity existingTopic = topicRepository.findById(topicId);
        if (existingTopic == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "话题不存在");
        }

        // 获取需要删除的图片
        List<String> oldImages = existingTopic.getImages();
        if (oldImages != null) {
            List<String> deletedImages = oldImages.stream()
                    .filter(url -> !imageUrls.contains(url))
                    .collect(Collectors.toList());

            // 删除不再使用的图片
            if (!deletedImages.isEmpty()) {
                minioService.deleteTopicImages(deletedImages);
            }
        }

        // 更新话题图片
        existingTopic.setImages(imageUrls);
        topicRepository.update(existingTopic);
    }

    /**
     * 根据ID获取话题
     */
    public Topic getTopicById(Long id) {
        TopicEntity topicEntity = topicRepository.findById(id);
        return convertToTopic(topicEntity);
    }
} 