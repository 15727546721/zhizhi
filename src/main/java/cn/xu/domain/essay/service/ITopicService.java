package cn.xu.domain.essay.service;

import cn.xu.api.web.model.dto.essay.TopicQueryRequest;
import cn.xu.domain.essay.command.CreateTopicCommand;
import cn.xu.domain.essay.model.entity.TopicEntity;

import java.util.List;

/**
 * 话题分类服务接口
 */
public interface ITopicService {

    /**
     * 创建分类
     *
     * @param command 创建话题命令
     */
    void createTopic(CreateTopicCommand command);

    /**
     * 分页查询话题
     * @return
     */
    List<TopicEntity> getTopicsByPage(TopicQueryRequest request);
}