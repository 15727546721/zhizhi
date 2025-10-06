package cn.xu.domain.essay.service;

import cn.xu.api.web.model.dto.essay.EssayQueryRequest;
import cn.xu.api.web.model.dto.essay.TopicResponse;
import cn.xu.domain.essay.command.CreateEssayCommand;
import cn.xu.domain.essay.model.entity.EssayEntity;
import cn.xu.domain.essay.model.vo.EssayResponse;

import java.util.List;

/**
 * 话题服务接口
 */
public interface IEssayService {
    /**
     * 创建话题
     */
    void createTopic(CreateEssayCommand command);

    /**
     * 根据查询条件获取话题列表
     *
     * @param request
     * @return
     */
    List<EssayResponse> getEssayList(EssayQueryRequest request);

    /**
     * 删除话题
     */
    void deleteTopic(Long id);

    /**
     * 批量删除话题
     */
    void deleteTopics(List<Long> ids);

    /**
     * 根据ID获取随笔详情
     *
     * @param essayId
     * @return
     */
    EssayEntity getEssayById(Long essayId);

    /**
     * 获取话题详情
     */
    TopicResponse getTopicDetail(Long id);

}