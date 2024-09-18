package cn.xu.controller.category;

import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.repository.ICategoryRepository;
import cn.xu.dto.category.CategoryDTO;
import cn.xu.dto.common.PageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RequestMapping("/category")
@RestController
public class CategoryController {

    @Resource
    private ICategoryRepository categoryRepository;

    @PostMapping("/add")
    public ResponseEntity addCategory(@RequestBody CategoryDTO categoryDTO) {

        if (ObjectUtils.isEmpty(categoryDTO)) {
            return ResponseEntity.builder()
                    .code(Constants.ResponseCode.NULL_PARAMETER.getCode())
                    .info(Constants.ResponseCode.NULL_PARAMETER.getInfo())
                    .build();
        }
        CategoryEntity categoryEntity = CategoryEntity.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .build();
        categoryRepository.save(categoryEntity);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("保存分类成功")
                .build();
    }

    @GetMapping("/list")
    public ResponseEntity getCategoryList(@ModelAttribute PageDTO pageDTO) {
        int page = pageDTO.getPage();
        int size = pageDTO.getSize();
        log.info("查询分类列表: page={}, size={}", page, size);
        List<CategoryEntity> categoryEntity = categoryRepository.queryCategoryList(page, size);
        return ResponseEntity.<List<CategoryEntity>>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("查询分类列表成功")
                .data(categoryEntity)
                .build();
    }

    @PostMapping("/update")
    public ResponseEntity updateCategory(@RequestBody CategoryDTO categoryDTO) {
        if (ObjectUtils.isEmpty(categoryDTO)) {
            return ResponseEntity.builder()
                    .code(Constants.ResponseCode.NULL_PARAMETER.getCode())
                    .info(Constants.ResponseCode.NULL_PARAMETER.getInfo())
                    .build();
        }
        CategoryEntity categoryEntity = CategoryEntity.builder()
                .id(categoryDTO.getId())
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .build();
        categoryRepository.update(categoryEntity);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("更新分类成功")
                .build();
    }

    @PostMapping("/delete")
    public ResponseEntity deleteCategory(@RequestBody List<Long> idList) {
        if (idList.isEmpty()) {
            return ResponseEntity.builder()
                    .code(Constants.ResponseCode.NULL_PARAMETER.getCode())
                    .info(Constants.ResponseCode.NULL_PARAMETER.getInfo())
                    .build();
        }
        categoryRepository.delete(idList);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("删除分类成功")
                .build();
    }
}
