package cn.xu.api.system.controller;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.topic.command.CreateTopicCategoryCommand;
import cn.xu.domain.topic.model.entity.TopicCategoryEntity;
import cn.xu.domain.topic.service.ITopicCategoryService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Validated
@Tag(name = "话题分类管理", description = "话题分类管理相关接口")
@RestController
@RequestMapping("/system/topic-category")
public class SysTopicCategoryController {

    @Resource
    private ITopicCategoryService topicCategoryService;

    @Operation(summary = "创建话题分类")
    @PostMapping("/create")
    public ResponseEntity<Void> createCategory(@RequestBody @Valid CreateTopicCategoryCommand command) {
        log.info("创建话题分类: {}", command);
        topicCategoryService.createCategory(command);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    @Operation(summary = "更新话题分类")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable @NotNull(message = "分类ID不能为空") Long id,
            @RequestBody @Valid CreateTopicCategoryCommand command) {
        log.info("更新话题分类, id: {}, category: {}", id, command);
        
        // 检查分类是否存在
        TopicCategoryEntity existingCategory = topicCategoryService.getCategory(id);
        if (existingCategory == null) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "话题分类不存在");
        }
        
        topicCategoryService.updateCategory(id, command);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    @Operation(summary = "删除话题分类")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable @NotNull(message = "分类ID不能为空") Long id) {
        log.info("删除话题分类, id: {}", id);
        
        // 检查分类是否存在
        TopicCategoryEntity existingCategory = topicCategoryService.getCategory(id);
        if (existingCategory == null) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "话题分类不存在");
        }
        
        topicCategoryService.deleteCategory(id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    @Operation(summary = "获取话题分类详情")
    @GetMapping("/{id}")
    public ResponseEntity<TopicCategoryEntity> getCategory(@PathVariable @NotNull(message = "分类ID不能为空") Long id) {
        log.info("获取话题分类详情, id: {}", id);
        TopicCategoryEntity category = topicCategoryService.getCategory(id);
        if (category == null) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "话题分类不存在");
        }
        return ResponseEntity.<TopicCategoryEntity>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(category)
                .build();
    }

    @Operation(summary = "获取所有话题分类")
    @GetMapping("/list")
    public ResponseEntity<List<TopicCategoryEntity>> getAllCategories() {
        log.info("获取所有话题分类");
        List<TopicCategoryEntity> categories = topicCategoryService.getAllCategories();
        return ResponseEntity.<List<TopicCategoryEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(categories)
                .build();
    }

    @Operation(summary = "根据名称搜索话题分类")
    @GetMapping("/search")
    public ResponseEntity<List<TopicCategoryEntity>> searchCategories(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword) {
        log.info("搜索话题分类, keyword: {}", keyword);
        List<TopicCategoryEntity> categories = topicCategoryService.getAllCategories();
        // 在内存中进行关键词过滤
        List<TopicCategoryEntity> filteredCategories = categories.stream()
                .filter(category -> category.getName().contains(keyword))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.<List<TopicCategoryEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(filteredCategories)
                .build();
    }
}
