package cn.xu.api.controller.web.category;


import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.service.ICategoryService;
import cn.xu.infrastructure.common.ResponseCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryApiController {

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
