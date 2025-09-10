package cn.xu.domain.essay.service.impl;

import cn.xu.api.web.model.dto.essay.TopicQueryRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.essay.command.CreateTopicCommand;
import cn.xu.domain.essay.model.entity.TopicEntity;
import cn.xu.domain.essay.repository.ITopicRepository;
import cn.xu.domain.essay.service.ITopicService;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class TopicServiceImpl implements ITopicService {

    @Resource
    private ITopicRepository topicRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTopic(CreateTopicCommand command) {
        // 1. 检查名称是否已存在
        TopicEntity existingCategory = topicRepository.findByName(command.getName());
        if (existingCategory != null) {
            throw new BusinessException(ResponseCode.DUPLICATE_KEY.getCode(), "分类名称已存在");
        }

        // 2. 创建分类实体
        TopicEntity categoryEntity = TopicEntity.builder()
                .name(command.getName())
                .build();


        Long topicId = topicRepository.save(categoryEntity);
        
    }

    @Override
    public List<TopicEntity> getTopicsByPage(TopicQueryRequest request) {
        List<TopicEntity> topics = topicRepository.getPageByName(request);
        return topics;
    }

} 