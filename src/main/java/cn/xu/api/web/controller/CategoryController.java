package cn.xu.api.web.controller;

import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.post.model.entity.CategoryEntity;
import cn.xu.domain.post.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/category")
@Tag(name = "帖子分类接口", description = "帖子分类相关接口")
public class CategoryController {

    @Resource
    private ICategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "获取分类列表", description = "获取所有可用的分类列表")
    @ApiOperationLog(description = "获取分类列表")
    public ResponseEntity<List<CategoryEntity>> getCategoryList() {
        try {
            List<CategoryEntity> categoryList = categoryService.getCategoryList();
            return ResponseEntity.<List<CategoryEntity>>builder()
                    .data(categoryList)
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取分类列表成功")
                    .build();
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return ResponseEntity.<List<CategoryEntity>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取分类列表失败")
                    .build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索分类", description = "根据关键词搜索分类")
    @ApiOperationLog(description = "搜索分类")
    public ResponseEntity<List<CategoryEntity>> searchCategories(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        try {
            List<CategoryEntity> categoryList = categoryService.searchCategories(keyword);
            return ResponseEntity.<List<CategoryEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("搜索分类成功")
                    .data(categoryList)
                    .build();
        } catch (Exception e) {
            log.error("搜索分类失败", e);
            return ResponseEntity.<List<CategoryEntity>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("搜索分类失败")
                    .build();
        }
    }

    @GetMapping("/{categoryId}/posts")
    @Operation(summary = "获取分类相关帖子", description = "获取指定分类的帖子列表")
    @ApiOperationLog(description = "获取分类相关帖子")
    public ResponseEntity<List<Object>> getCategoryPosts(
            @Parameter(description = "分类ID") @PathVariable Long categoryId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            // 这里需要实现获取分类相关帖子的逻辑
            // List<Object> posts = categoryService.getCategoryPosts(categoryId, pageNo, pageSize);
            return ResponseEntity.<List<Object>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取分类相关帖子成功")
                    .data(null) // 暂时返回null，需要实现具体逻辑
                    .build();
        } catch (Exception e) {
            log.error("获取分类相关帖子失败", e);
            return ResponseEntity.<List<Object>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取分类相关帖子失败")
                    .build();
        }
    }

    @GetMapping("/{categoryId}/stats")
    @Operation(summary = "获取分类统计信息", description = "获取分类的统计信息")
    @ApiOperationLog(description = "获取分类统计信息")
    public ResponseEntity<Object> getCategoryStats(
            @Parameter(description = "分类ID") @PathVariable Long categoryId) {
        try {
            // 这里需要实现获取分类统计信息的逻辑
            // Object stats = categoryService.getCategoryStats(categoryId);
            return ResponseEntity.<Object>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取分类统计信息成功")
                    .data(null) // 暂时返回null，需要实现具体逻辑
                    .build();
        } catch (Exception e) {
            log.error("获取分类统计信息失败", e);
            return ResponseEntity.<Object>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取分类统计信息失败")
                    .build();
        }
    }

    @GetMapping("/tree")
    @Operation(summary = "获取分类树", description = "获取分类的树形结构")
    @ApiOperationLog(description = "获取分类树")
    public ResponseEntity<List<Object>> getCategoryTree() {
        try {
            // 这里需要实现获取分类树的逻辑
            // List<Object> tree = categoryService.getCategoryTree();
            return ResponseEntity.<List<Object>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取分类树成功")
                    .data(null) // 暂时返回null，需要实现具体逻辑
                    .build();
        } catch (Exception e) {
            log.error("获取分类树失败", e);
            return ResponseEntity.<List<Object>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取分类树失败")
                    .build();
        }
    }
}