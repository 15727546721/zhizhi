package cn.xu.api;

import cn.xu.api.model.category.CategoryDTO;
import cn.xu.api.model.common.PageDTO;
import cn.xu.types.model.ResponseEntity;

import java.util.List;

public interface ICategoryServiceController {
    ResponseEntity addCategory(CategoryDTO categoryDTO);

    ResponseEntity getCategoryList(PageDTO pageDTO);

    ResponseEntity updateCategory(CategoryDTO categoryDTO);

    ResponseEntity deleteCategory(List<Long> idList);
}
