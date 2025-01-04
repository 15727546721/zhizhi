package cn.xu.api.controller.web.topic;

import cn.xu.api.dto.topic.TopicDTO;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.topic.command.CreateTopicCommand;
import cn.xu.domain.topic.entity.Topic;
import cn.xu.domain.topic.model.entity.TopicCategoryEntity;
import cn.xu.domain.topic.service.ITopicCategoryService;
import cn.xu.domain.topic.service.TopicService;
import cn.xu.infrastructure.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/topic")
@Tag(name = "话题接口")
public class TopicController {

    @Resource
    private TopicService topicService;
    
    @Resource
    private ITopicCategoryService topicCategoryService;

    @Operation(summary = "创建话题")
    @PostMapping("/create")
    public ResponseEntity<TopicDTO> createTopic(@RequestBody CreateTopicCommand command) {
        Topic topic = topicService.createTopic(command);
        return ResponseEntity.<TopicDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(convertToDTO(topic))
                .build();
    }

    @Operation(summary = "获取所有话题列表")
    @GetMapping("/list")
    public ResponseEntity<List<TopicDTO>> getAllTopics() {
        log.info("获取所有话题列表");
        List<Topic> topics = topicService.getAllTopics();
        log.info("获取所有话题列表: " + topics);
        List<TopicDTO> topicDTOs = topics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.<List<TopicDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(topicDTOs)
                .build();
    }

    @Operation(summary = "获取话题列表")
    @GetMapping("/{id}")
    public ResponseEntity<List<TopicDTO>> getTopicList(@PathVariable("id") Long categoryId) {
        List<Topic> topics = topicService.getTopicList(categoryId);
        log.info("获取话题列表: " + topics);
        List<TopicDTO> topicDTOs = topics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.<List<TopicDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(topicDTOs)
                .build();
    }

    @Operation(summary = "获取热门话题")
    @GetMapping("/hot")
    public ResponseEntity<List<TopicDTO>> getHotTopics(@RequestParam(defaultValue = "10") int limit) {
        List<Topic> topics = topicService.getHotTopics(limit);
        List<TopicDTO> topicDTOs = topics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.<List<TopicDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(topicDTOs)
                .build();
    }

    @Operation(summary = "删除话题")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    /**
     * 将Topic转换为TopicDTO
     */
    private TopicDTO convertToDTO(Topic topic) {
        if (topic == null) {
            return null;
        }
        
        // 获取分类名称
        String categoryName = null;
        if (topic.getCategoryId() != null) {
            TopicCategoryEntity category = topicCategoryService.getCategory(topic.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }
        
        return TopicDTO.builder()
                .id(topic.getId())
                .userId(topic.getUserId())
                .content(topic.getContent())
                .images(topic.getImages())
                .categoryId(topic.getCategoryId())
                .categoryName(categoryName)
                .createTime(topic.getCreateTime())
                .updateTime(topic.getUpdateTime())
                .build();
    }
} 