package cn.xu.api.controller.web.topic;

import cn.xu.api.dto.common.ResponseEntity;
import cn.xu.api.dto.topic.TopicCategoryDTO;
import cn.xu.domain.topic.command.CreateTopicCategoryCommand;
import cn.xu.domain.topic.model.entity.TopicCategoryEntity;
import cn.xu.domain.topic.service.ITopicCategoryService;
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
@RequestMapping("/api/topic/categories")
@Tag(name = "话题分类接口")
public class TopicCategoryController {

    @Resource
    private ITopicCategoryService topicCategoryService;

    @Operation(summary = "创建话题分类")
    @PostMapping("/create")
    public ResponseEntity<TopicCategoryDTO> createCategory(@RequestBody CreateTopicCategoryCommand command) {
        TopicCategoryEntity category = topicCategoryService.createCategory(command);
        return ResponseEntity.<TopicCategoryDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(convertToDTO(category))
                .build();
    }

    @Operation(summary = "更新话题分类")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCategory(@PathVariable Long id, @RequestBody CreateTopicCategoryCommand command) {
        topicCategoryService.updateCategory(id, command);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    @Operation(summary = "删除话题分类")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        topicCategoryService.deleteCategory(id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    @Operation(summary = "获取话题分类")
    @GetMapping("/{id}")
    public ResponseEntity<TopicCategoryDTO> getCategory(@PathVariable Long id) {
        TopicCategoryEntity category = topicCategoryService.getCategory(id);
        return ResponseEntity.<TopicCategoryDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(convertToDTO(category))
                .build();
    }

    @Operation(summary = "获取所有话题分类")
    @GetMapping("/list")
    public ResponseEntity<List<TopicCategoryDTO>> getAllCategories() {
        List<TopicCategoryEntity> categories = topicCategoryService.getAllCategories();
        log.info("获取所有话题分类: {}", categories);
        List<TopicCategoryDTO> categoryDTOs = categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.<List<TopicCategoryDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(categoryDTOs)
                .build();
    }

    /**
     * 将领域实体转换为DTO
     */
    private TopicCategoryDTO convertToDTO(TopicCategoryEntity category) {
        if (category == null) {
            return null;
        }
        return TopicCategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .sort(category.getSort())
                .createTime(category.getCreateTime())
                .updateTime(category.getUpdateTime())
                .build();
    }
} 