package cn.xu.domain.topic.service;

import cn.xu.api.web.model.dto.topic.TopicResponse;
import cn.xu.api.web.model.dto.topic.TopicUpdateRequest;
import cn.xu.domain.topic.command.CreateTopicCommand;
import cn.xu.domain.topic.entity.Topic;
import cn.xu.infrastructure.common.request.PageRequest;

import java.util.List;

/**
 * 话题服务接口
 */
public interface ITopicService {
    /**
     * 创建话题
     */
    Topic createTopic(CreateTopicCommand command);

    /**
     * 更新话题
     */
    void updateTopic(Long id, TopicUpdateRequest request);

    /**
     * 删除话题
     */
    void deleteTopic(Long id);

    /**
     * 批量删除话题
     */
    void deleteTopics(List<Long> ids);

    /**
     * 获取话题列表
     */
    List<Topic> getTopicList(Long categoryId);

    /**
     * 获取热门话题
     */
    List<Topic> getHotTopics(int limit);

    /**
     * 获取所有话题列表
     */
    List<Topic> getAllTopics();

    /**
     * 根据ID获取话题
     */
    Topic getTopicById(Long id);

    /**
     * 分页查询话题列表
     */
    List<TopicResponse> getTopics(PageRequest pageRequest);

    /**
     * 获取话题详情
     */
    TopicResponse getTopicDetail(Long id);

    /**
     * 获取话题作者ID
     *
     * @param topicId 话题ID
     * @return 作者ID，如果话题不存在则返回null
     */
    Long getTopicAuthorId(Long topicId);
} 