package cn.xu.api.web.controller;

import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.request.PageRequest;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.post.model.entity.TopicEntity;
import cn.xu.domain.post.service.IPostTopicService;
import cn.xu.domain.post.service.ITopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 话题控制器
 * 提供话题相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/topic")
@Tag(name = "话题接口", description = "话题相关接口")
public class TopicController {

    @Resource
    private ITopicService topicService;
    @Resource
    private IPostTopicService postTopicService;

    /**
     * 创建话题
     *
     * @param topicName 话题名称
     * @return 响应结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建话题")
    @ApiOperationLog(description = "创建话题")
    public ResponseEntity createTopic(@Parameter(description = "话题名称") @RequestParam String topicName) {
        try {
            topicService.addTopic(topicName);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("创建话题成功")
                    .build();
        } catch (Exception e) {
            log.error("创建话题失败", e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("创建话题失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 搜索话题
     *
     * @param keyword 搜索关键词
     * @return 话题列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索话题")
    @ApiOperationLog(description = "搜索话题")
    public ResponseEntity<List<TopicEntity>> searchTopics(@Parameter(description = "搜索关键词") @RequestParam String keyword) {
        try {
            List<TopicEntity> topics = topicService.searchTopics(keyword);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("搜索话题成功")
                    .data(topics)
                    .build();
        } catch (Exception e) {
            log.error("搜索话题失败", e);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索话题失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取热门话题
     *
     * @param limit 限制数量
     * @return 热门话题列表
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门话题")
    @ApiOperationLog(description = "获取热门话题")
    public ResponseEntity<List<TopicEntity>> getHotTopics(@Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<TopicEntity> topics = topicService.getHotTopics(limit);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取热门话题成功")
                    .data(topics)
                    .build();
        } catch (Exception e) {
            log.error("获取热门话题失败", e);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取热门话题失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取所有话题（分页）
     *
     * @param pageRequest 分页请求
     * @return 话题列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有话题")
    @ApiOperationLog(description = "获取所有话题")
    public ResponseEntity<List<TopicEntity>> getAllTopics(PageRequest pageRequest) {
        try {
            List<TopicEntity> topics = topicService.getAllTopics();
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取话题列表成功")
                    .data(topics)
                    .build();
        } catch (Exception e) {
            log.error("获取话题列表失败", e);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取话题列表失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取帖子相关的话题详情
     *
     * @param postId 帖子ID
     * @return 话题详情列表
     */
    @GetMapping("/post/{postId}")
    @Operation(summary = "获取帖子相关的话题详情")
    @ApiOperationLog(description = "获取帖子相关的话题详情")
    public ResponseEntity<List<TopicEntity>> getTopicsByPostId(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        try {
            // 获取帖子关联的话题ID列表
            List<Long> topicIds = postTopicService.getTopicsByPostId(postId);
            
            // 根据话题ID获取话题详情
            List<TopicEntity> topics = new ArrayList<>();
            for (Long topicId : topicIds) {
                TopicEntity topic = topicService.getTopicById(topicId);
                if (topic != null) {
                    topics.add(topic);
                }
            }
            
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取帖子话题成功")
                    .data(topics)
                    .build();
        } catch (Exception e) {
            log.error("获取帖子话题失败", e);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子话题失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 为帖子设置话题
     *
     * @param postId 帖子ID
     * @param topicIds 话题ID列表
     * @return 响应结果
     */
    @PostMapping("/post/{postId}")
    @Operation(summary = "为帖子设置话题")
    @ApiOperationLog(description = "为帖子设置话题")
    public ResponseEntity setPostTopics(@Parameter(description = "帖子ID") @PathVariable Long postId, @Parameter(description = "话题ID列表") @RequestBody List<Long> topicIds) {
        try {
            postTopicService.savePostTopics(postId, topicIds);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("设置帖子话题成功")
                    .build();
        } catch (Exception e) {
            log.error("设置帖子话题失败", e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("设置帖子话题失败: " + e.getMessage())
                    .build();
        }
    }
}