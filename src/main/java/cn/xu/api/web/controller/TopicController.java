package cn.xu.api.web.controller;

import cn.xu.api.web.model.vo.topic.TopicVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.file.service.MinioService;
import cn.xu.domain.topic.command.CreateTopicCommand;
import cn.xu.domain.topic.entity.Topic;
import cn.xu.domain.topic.model.entity.TopicCategoryEntity;
import cn.xu.domain.topic.service.ITopicCategoryService;
import cn.xu.domain.topic.service.TopicService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
    public ResponseEntity<TopicVO> createTopic(@RequestBody CreateTopicCommand command) {
        Topic topic = topicService.createTopic(command);
        return ResponseEntity.<TopicVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(convertToDTO(topic))
                .build();
    }

    @Operation(summary = "获取话题列表")
    @GetMapping("/list")
    public ResponseEntity<List<TopicVO>> getTopics(@RequestParam(value = "categoryId", required = false) Long categoryId) {
        List<Topic> topics;
        if (categoryId == null) {
            log.info("获取所有话题列表");
            topics = topicService.getAllTopics();
        } else {
            log.info("获取分类ID为 " + categoryId + " 的话题列表");
            topics = topicService.getTopicList(categoryId);
        }
        log.info("获取话题列表: " + topics);
        List<TopicVO> topicVOS = topics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.<List<TopicVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(topicVOS)
                .build();
    }

    @Operation(summary = "获取热门话题")
    @GetMapping("/hot")
    public ResponseEntity<List<TopicVO>> getHotTopics(@RequestParam(defaultValue = "10") int limit) {
        List<Topic> topics = topicService.getHotTopics(limit);
        List<TopicVO> topicVOS = topics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.<List<TopicVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(topicVOS)
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

    @Operation(summary = "获取分页话题列表")
    @Parameters({
        @Parameter(name = "pageNum", description = "页码", required = true),
        @Parameter(name = "pageSize", description = "每页数量", required = true),
        @Parameter(name = "categoryId", description = "分类ID"),
        @Parameter(name = "keyword", description = "搜索关键词")
    })
    @GetMapping("/list/page")
    public ResponseEntity<List<TopicVO>> getTopicsPage(@RequestParam(value = "pageNum", required = true) int pageNum,
                                                       @RequestParam(value = "pageSize", required = true) int pageSize,
                                                       @RequestParam(value = "categoryId", required = false) Long categoryId,
                                                       @RequestParam(value = "keyword", required = false) String keyword) {
        List<Topic> topics;
        if (categoryId == null) {
            log.info("获取所有话题列表");
            topics = topicService.getAllTopics();
        } else {
            log.info("获取分类ID为 " + categoryId + " 的话题列表");
            topics = topicService.getTopicList(categoryId);
        }
        log.info("获取话题列表: " + topics);
        List<TopicVO> topicVOS = topics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.<List<TopicVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(topicVOS)
                .build();
    }

    /**
     * 将Topic转换为TopicDTO
     */
    private TopicVO convertToDTO(Topic topic) {
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

        return TopicVO.builder()
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