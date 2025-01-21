package cn.xu.api.system.controller;

import cn.xu.api.web.model.dto.topic.TopicResponse;
import cn.xu.api.web.model.dto.topic.TopicUpdateRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.topic.command.TopicPageQuery;
import cn.xu.domain.topic.service.TopicService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.request.PageRequest;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@Tag(name = "话题管理", description = "话题管理相关接口")
@RestController
@RequestMapping("/system/topic")
public class SysTopicController {

    @Resource
    private TopicService topicService;

    /**
     * 获取分页话题列表
     *
     * @param pageRequest 分页请求参数
     * @return 话题列表
     */
    @Operation(summary = "获取分页话题列表")
    @Parameters({
        @Parameter(name = "pageNum", description = "页码", required = true),
        @Parameter(name = "pageSize", description = "每页数量", required = true),
        @Parameter(name = "categoryId", description = "分类ID"),
        @Parameter(name = "keyword", description = "搜索关键词")
    })
    @GetMapping("/list")
    public ResponseEntity<List<TopicResponse>> getTopics(@Valid PageRequest pageRequest,
                                                        @RequestParam(required = false) Long categoryId,
                                                        @RequestParam(required = false) String keyword) {
        log.info("获取分页话题列表: pageRequest={}, categoryId={}, keyword={}", pageRequest, categoryId, keyword);
        
        TopicPageQuery query = TopicPageQuery.builder()
                .pageNum(pageRequest.getPageNo())
                .pageSize(pageRequest.getPageSize())
                .build();
                
        List<TopicResponse> topics = topicService.getTopics(pageRequest);
        return ResponseEntity.<List<TopicResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(topics)
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
    public ResponseEntity<TopicResponse> getTopicDetail(@PathVariable @NotNull(message = "话题ID不能为空") Long id) {
        log.info("获取话题详情, id: {}", id);
        TopicResponse topic = topicService.getTopicDetail(id);
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
     * 更新话题信息
     *
     * @param id      话题ID
     * @param request 更新请求参数
     */
    @Operation(summary = "更新话题信息")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTopic(@PathVariable @NotNull(message = "话题ID不能为空") Long id,
                                          @RequestBody @Valid TopicUpdateRequest request) {
        log.info("更新话题信息, id: {}, request: {}", id, request);
        
        // 检查话题是否存在
        TopicResponse existingTopic = topicService.getTopicDetail(id);
        if (existingTopic == null) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "话题不存在");
        }
        
        topicService.updateTopic(id, request);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    /**
     * 删除话题
     *
     * @param id 话题ID
     */
    @Operation(summary = "删除话题")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable @NotNull(message = "话题ID不能为空") Long id) {
        log.info("删除话题, id: {}", id);
        
        // 检查话题是否存在
        TopicResponse existingTopic = topicService.getTopicDetail(id);
        if (existingTopic == null) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "话题不存在");
        }
        
        topicService.deleteTopic(id);
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
    public ResponseEntity<Void> batchDeleteTopics(@RequestBody @NotEmpty(message = "话题ID列表不能为空") List<Long> ids) {
        log.info("批量删除话题, ids: {}", ids);
        
        if (ids.isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "话题ID列表不能为空");
        }
        
        // 检查所有话题是否存在
        for (Long id : ids) {
            TopicResponse existingTopic = topicService.getTopicDetail(id);
            if (existingTopic == null) {
                throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "话题ID " + id + " 不存在");
            }
        }
        
        // 改为循环删除
        for (Long id : ids) {
            topicService.deleteTopic(id);
        }
        
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    /**
     * 获取话题统计信息
     *
     * @return 统计信息
     */
    @Operation(summary = "获取话题统计信息")
    @Parameters({
        @Parameter(name = "startDate", description = "开始日期 (yyyy-MM-dd)"),
        @Parameter(name = "endDate", description = "结束日期 (yyyy-MM-dd)")
    })
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTopicStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取话题统计信息, startDate: {}, endDate: {}", startDate, endDate);
        
        // 创建一个新的PageRequest对象
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(Integer.MAX_VALUE);
        
        // 获取基础统计信息
        long totalCount = topicService.getTopics(pageRequest).size();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", totalCount);
        statistics.put("startDate", startDate);
        statistics.put("endDate", endDate);
        
        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(statistics)
                .build();
    }
}
