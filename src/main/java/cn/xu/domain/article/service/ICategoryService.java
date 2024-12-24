package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.CategoryEntity;

import java.util.List;

public interface ICategoryService {

    List<CategoryEntity> queryCategoryList(int page, int size);

    public void save(CategoryEntity category);

    public void update(CategoryEntity categoryEntity);

    public void delete(List<Long> idList);

    List<CategoryEntity> getCategorySelect();

    CategoryEntity getCategoryByArticleId(Long id);

    List<CategoryEntity> getCategoryList();
}
