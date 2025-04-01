package cn.xu.api.web.controller;

import cn.xu.api.web.model.dto.essay.TopicQueryRequest;
import cn.xu.api.web.model.vo.topic.TopicVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.essay.command.CreateTopicCommand;
import cn.xu.domain.essay.model.entity.TopicEntity;
import cn.xu.domain.essay.service.ITopicService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/topic/")
@Tag(name = "话题分类接口")
public class TopicController {

    @Resource
    private ITopicService topicService;

    @Operation(summary = "创建话题")
    @PostMapping("/create")
    public ResponseEntity<TopicVO> createCategory(@RequestBody CreateTopicCommand command) {
        if (command == null || StringUtils.isEmpty(command.getName())) {
            throw new BusinessException("话题名称不能为空");
        }
        topicService.createTopic(command);
        return ResponseEntity.<TopicVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }


    @Operation(summary = "分页查询话题")
    @PostMapping("/list/page")
    public ResponseEntity<List<TopicEntity>> getTopicsByPage(@RequestBody TopicQueryRequest request) {
        List<TopicEntity> topics = topicService.getTopicsByPage(request);
        return ResponseEntity.<List<TopicEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(topics)
                .build();
    }

} 