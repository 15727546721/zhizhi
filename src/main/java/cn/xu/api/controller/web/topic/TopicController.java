package cn.xu.api.controller.web.topic;

import cn.xu.api.dto.topic.TopicDTO;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.file.service.MinioService;
import cn.xu.domain.topic.command.CreateTopicCommand;
import cn.xu.domain.topic.entity.Topic;
import cn.xu.domain.topic.model.entity.TopicCategoryEntity;
import cn.xu.domain.topic.service.ITopicCategoryService;
import cn.xu.domain.topic.service.TopicService;
import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    @Resource
    private MinioService minioService;

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

    @Operation(summary = "上传话题图片")
    @PostMapping("/upload/images")
    public ResponseEntity<List<String>> uploadTopicImages(@RequestParam("files") MultipartFile[] files) {
        log.info("开始上传话题图片，文件数量: {}", files.length);
        List<String> imageUrls = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                // 检查文件类型
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "只能上传图片文件");
                }

                // 生成临时文件存储路径 (temp/年月日/时间戳_文件名)
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String filePath = String.format("temp/%s/%s_%s",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                        timestamp,
                        UUID.randomUUID());

                // 上传文件并获取URL
                String imageUrl = minioService.uploadTopicImage(file, filePath);
                imageUrls.add(imageUrl);
            }

            return ResponseEntity.<List<String>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(imageUrls)
                    .build();
        } catch (Exception e) {
            // 上传失败时，删除已上传的图片
            if (!imageUrls.isEmpty()) {
                minioService.deleteTopicImages(imageUrls);
            }
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传图片失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除话题图片")
    @DeleteMapping("/images")
    public ResponseEntity<Void> deleteTopicImages(
            @RequestParam("urls") List<String> imageUrls,
            @RequestParam(value = "topicId", required = false) Long topicId) {
        try {
            // 如果指定了话题ID，更新话题的图片列表
            if (topicId != null) {
                Topic topic = topicService.getTopicById(topicId);
                if (topic != null) {
                    List<String> remainingImages = topic.getImages().stream()
                            .filter(url -> !imageUrls.contains(url))
                            .collect(Collectors.toList());
                    topicService.updateTopicImages(topicId, remainingImages);
                }
            }

            // 删除图片文件
            minioService.deleteTopicImages(imageUrls);

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .build();
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除图片失败：" + e.getMessage());
        }
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