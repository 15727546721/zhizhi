package cn.xu.trigger.http.category;

import cn.xu.api.ICategoryServiceController;
import cn.xu.api.model.category.CategoryDTO;
import cn.xu.api.model.common.PageDTO;
import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.repository.ICategoryRepository;
import cn.xu.types.common.Constants;
import cn.xu.types.model.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RequestMapping("/category")
@RestController
public class CategoryController implements ICategoryServiceController {

    @Resource
    private ICategoryRepository categoryRepository;

    @PostMapping("/add")
    @Override
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
    @Override
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
}
