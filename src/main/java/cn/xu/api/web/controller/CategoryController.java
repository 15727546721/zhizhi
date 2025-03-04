package cn.xu.api.web.controller;


import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.service.ICategoryService;
import cn.xu.infrastructure.common.response.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Resource
    private ICategoryService categoryService;

    @GetMapping("/list")
    public ResponseEntity<List<CategoryEntity>> getCategoryList() {
        List<CategoryEntity> categoryList = categoryService.getCategoryList();
        return ResponseEntity.<List<CategoryEntity>>builder()
                .data(categoryList)
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }
}
