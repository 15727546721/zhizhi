package cn.xu.api.system.controller;

import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.essay.command.CreateTopicCommand;
import cn.xu.domain.essay.service.ITopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@Validated
@Tag(name = "话题管理", description = "话题相关接口")
@RestController
@RequestMapping("/system/topic")
public class SysTopicController {

    @Resource
    private ITopicService topicService;

    @Operation(summary = "创建话题")
    @PostMapping("/create")
    @ApiOperationLog(description = "创建话题")
    public ResponseEntity<Void> createTopic(@RequestBody @Valid CreateTopicCommand command) {
        log.info("创建话题: {}", command);
        topicService.createTopic(command);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

}