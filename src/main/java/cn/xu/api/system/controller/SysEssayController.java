package cn.xu.api.system.controller;

import cn.xu.api.web.model.dto.essay.TopicResponse;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.essay.command.CreateEssayCommand;
import cn.xu.domain.essay.service.impl.EssayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Validated
@Tag(name = "话题管理", description = "话题管理相关接口")
@RestController
@RequestMapping("/system/essay")
public class SysEssayController {

    @Resource
    private EssayService essayService;

    @Operation(summary = "创建随笔")
    @PostMapping("/create")
    @ApiOperationLog(description = "创建随笔")
    public ResponseEntity<Void> createTopic(@RequestBody CreateEssayCommand command) {
        essayService.createTopic(command);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }
    /**
     * 获取话题详情
     *
     * @param id 话题ID
     * @return 话题详情
     */
    @Operation(summary = "获取话题详情")
    @GetMapping("/{id}")
    @ApiOperationLog(description = "获取话题详情")
    public ResponseEntity<TopicResponse> getTopicDetail(@Parameter(description = "话题ID") @PathVariable @NotNull(message = "话题ID不能为空") Long id) {
        log.info("获取话题详情, id: {}", id);
        TopicResponse topic = essayService.getTopicDetail(id);
        if (topic == null) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "话题不存在");
        }
        return ResponseEntity.<TopicResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(topic)
                .build();
    }



    /**
     * 删除话题
     *
     * @param id 话题ID
     */
    @Operation(summary = "删除话题")
    @DeleteMapping("/{id}")
    @ApiOperationLog(description = "删除话题")
    public ResponseEntity<Void> deleteTopic(@Parameter(description = "话题ID") @PathVariable @NotNull(message = "话题ID不能为空") Long id) {
        log.info("删除话题, id: {}", id);
        
        // 检查话题是否存在
        TopicResponse existingTopic = essayService.getTopicDetail(id);
        if (existingTopic == null) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "话题不存在");
        }
        
        essayService.deleteTopic(id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    /**
     * 批量删除话题
     *
     * @param ids 话题ID列表
     */
    @Operation(summary = "批量删除话题")
    @DeleteMapping("/batch")
    @ApiOperationLog(description = "批量删除话题")
    public ResponseEntity<Void> batchDeleteTopics(@Parameter(description = "话题ID列表") @RequestBody @NotEmpty(message = "话题ID列表不能为空") List<Long> ids) {
        log.info("批量删除话题, ids: {}", ids);
        
        if (ids.isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "话题ID列表不能为空");
        }
        
        // 检查所有话题是否存在
        for (Long id : ids) {
            TopicResponse existingTopic = essayService.getTopicDetail(id);
            if (existingTopic == null) {
                throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "话题ID " + id + " 不存在");
            }
        }
        
        // 改为循环删除
        for (Long id : ids) {
            essayService.deleteTopic(id);
        }
        
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

}