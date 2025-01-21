package cn.xu.api.system.controller;

import cn.xu.api.system.model.dto.article.CategoryRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.service.ICategoryService;
import cn.xu.infrastructure.common.request.PageRequest;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "文章分类管理", description = "文章分类管理相关接口")
@Slf4j
@RequestMapping("system/category")
@RestController
public class SysCategoryController {

    @Resource
    private ICategoryService categoryService;

    @PostMapping("/add")
    public ResponseEntity addCategory(@RequestBody CategoryRequest categoryRequest) {

        if (ObjectUtils.isEmpty(categoryRequest)) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info(ResponseCode.NULL_PARAMETER.getMessage())
                    .build();
        }
        CategoryEntity categoryEntity = CategoryEntity.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .build();
        categoryService.save(categoryEntity);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("保存分类成功")
                .build();
    }

    @GetMapping("/list")
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
                .description(categoryRequest.getDescription())
                .build();
        categoryService.update(categoryEntity);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新分类成功")
                .build();
    }

    @PostMapping("/delete")
    public ResponseEntity deleteCategory(@RequestBody List<Long> idList) {
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
