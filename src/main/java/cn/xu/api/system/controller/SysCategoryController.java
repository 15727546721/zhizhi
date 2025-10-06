package cn.xu.api.system.controller;

import cn.xu.api.system.model.dto.post.CategoryRequest;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.request.PageRequest;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.post.model.entity.CategoryEntity;
import cn.xu.domain.post.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "帖子分类管理", description = "帖子分类管理相关接口")
@Slf4j
@RequestMapping("system/category")
@RestController
public class SysCategoryController {

    @Resource
    private ICategoryService categoryService;

    @PostMapping("/add")
    @Operation(summary = "添加分类", description = "添加新的帖子分类")
    @ApiOperationLog(description = "添加分类")
    public ResponseEntity addCategory(@RequestBody CategoryRequest categoryRequest) {

        if (ObjectUtils.isEmpty(categoryRequest)) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info(ResponseCode.NULL_PARAMETER.getMessage())
                    .build();
        }
        CategoryEntity categoryEntity = CategoryEntity.builder()
                .name(categoryRequest.getName())
                .build();
        categoryService.save(categoryEntity);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("保存分类成功")
                .build();
    }

    @GetMapping("/list")
    @Operation(summary = "获取分类列表", description = "分页获取帖子分类列表")
    @ApiOperationLog(description = "获取分类列表")
    public ResponseEntity getCategoryList(@ModelAttribute PageRequest pageRequest) {
        int page = pageRequest.getPageNo();
        int size = pageRequest.getPageSize();
        log.info("查询分类列表: page={}, size={}", page, size);
        List<CategoryEntity> categoryEntity = categoryService.queryCategoryList(page, size);
        return ResponseEntity.<List<CategoryEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询分类列表成功")
                .data(categoryEntity)
                .build();
    }

    @GetMapping("/getCategorySelect")
    @Operation(summary = "获取分类下拉列表", description = "获取所有分类的下拉列表选项")
    @ApiOperationLog(description = "获取分类下拉列表")
    public ResponseEntity getCategorySelect() {
        log.info("获取分类下拉列表");
        List<CategoryEntity> categoryEntity = categoryService.getCategorySelect();
        return ResponseEntity.<List<CategoryEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询分类列表成功")
                .data(categoryEntity)
                .build();
    }

    @PostMapping("/update")
    @Operation(summary = "更新分类", description = "更新帖子分类信息")
    @ApiOperationLog(description = "更新分类")
    public ResponseEntity updateCategory(@RequestBody CategoryRequest categoryRequest) {
        if (ObjectUtils.isEmpty(categoryRequest)) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info(ResponseCode.NULL_PARAMETER.getMessage())
                    .build();
        }
        CategoryEntity categoryEntity = CategoryEntity.builder()
                .id(categoryRequest.getId())
                .name(categoryRequest.getName())
                .build();
        categoryService.update(categoryEntity);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新分类成功")
                .build();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除分类", description = "删除指定的帖子分类")
    @ApiOperationLog(description = "删除分类")
    public ResponseEntity deleteCategory(@Parameter(description = "分类ID列表") @RequestBody List<Long> idList) {
        if (idList.isEmpty()) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info(ResponseCode.NULL_PARAMETER.getMessage())
                    .build();
        }
        categoryService.delete(idList);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除分类成功")
                .build();
    }
}