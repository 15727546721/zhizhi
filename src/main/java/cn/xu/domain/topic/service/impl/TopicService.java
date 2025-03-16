package cn.xu.domain.topic.service.impl;

import cn.xu.api.web.model.dto.topic.TopicResponse;
import cn.xu.api.web.model.dto.topic.TopicUpdateRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.file.service.MinioService;
import cn.xu.domain.topic.command.CreateTopicCommand;
import cn.xu.domain.topic.command.TopicPageQuery;
import cn.xu.domain.topic.command.TopicUpdateCommand;
import cn.xu.domain.topic.model.entity.Topic;
import cn.xu.domain.topic.model.entity.TopicEntity;
import cn.xu.domain.topic.repository.ITopicRepository;
import cn.xu.domain.topic.service.ITopicCategoryService;
import cn.xu.domain.topic.service.ITopicService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.request.PageRequest;
import cn.xu.infrastructure.persistent.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TopicService implements ITopicService {

    @Resource
    private ITopicRepository topicRepository;

    @Resource
    private ITopicCategoryService topicCategoryService;

    @Resource
    private MinioService minioService;

    @Resource
    private CommentRepository commentRepository;

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
        // 删除与话题相关的评论
        commentRepository.deleteByTopicId(id);

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

    /**
     * 分页查询话题列表
     *
     * @param pageRequest 分页请求参数
     * @return 话题响应列表
     */
    public List<TopicResponse> getTopics(PageRequest pageRequest) {
        TopicPageQuery query = TopicPageQuery.builder()
                .pageNum(pageRequest.getPageNo())
                .pageSize(pageRequest.getPageSize())
                .build();

        List<TopicEntity> topicEntities = topicRepository.findByPage(query.getOffset(), query.getLimit());
        return topicEntities.stream()
                .map(this::convertToTopicResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取话题详情
     *
     * @param id 话题ID
     * @return 话题响应
     */
    public TopicResponse getTopicDetail(Long id) {
        if (id == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "话题ID不能为空");
        }
        TopicEntity topicEntity = topicRepository.findById(id);
        if (topicEntity == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "话题不存在");
        }
        return convertToTopicResponse(topicEntity);
    }

    /**
     * 更新话题
     *
     * @param id      话题ID
     * @param request 更新请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTopic(Long id, TopicUpdateRequest request) {
        TopicUpdateCommand command = TopicUpdateCommand.builder()
                .id(id)
                .content(request.getContent())
                .images(request.getImages())
                .categoryId(request.getCategoryId())
                .build();

        command.validate();

        TopicEntity existingTopic = topicRepository.findById(id);
        if (existingTopic == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "话题不存在");
        }

        // 更新话题内容
        existingTopic.setContent(command.getContent());
        existingTopic.setImages(command.getImages());
        existingTopic.setCategoryId(command.getCategoryId());

        // 验证更新后的话题
        existingTopic.validate();

        // 保存更新
        topicRepository.update(existingTopic);
    }

    private TopicResponse convertToTopicResponse(TopicEntity topicEntity) {
        if (topicEntity == null) {
            return null;
        }
        return TopicResponse.builder()
                .id(topicEntity.getId())
                .userId(topicEntity.getUserId())
                .content(topicEntity.getContent())
                .images(topicEntity.getImages())
                .categoryId(topicEntity.getCategoryId())
                .createTime(topicEntity.getCreateTime())
                .updateTime(topicEntity.getUpdateTime())
                .build();
    }

    @Override
    public Long getTopicAuthorId(Long topicId) {
        try {
            log.debug("[话题服务] 获取话题作者ID - topicId: {}", topicId);
            TopicEntity topic = topicRepository.findById(topicId);
            if (topic == null) {
                log.warn("[话题服务] 话题不存在 - topicId: {}", topicId);
                return null;
            }
            return topic.getUserId();
        } catch (Exception e) {
            log.error("[话题服务] 获取话题作者ID失败 - topicId: {}", topicId, e);
            return null;
        }
    }
}