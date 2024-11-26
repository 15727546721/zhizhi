package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.entity.CategoryEntity;

import java.util.List;

public interface ICategoryRepository {
    void save(CategoryEntity category);

    List<CategoryEntity> queryCategoryList(int page, int size);

    void update(CategoryEntity categoryEntity);

    void delete(List<Long> idList);

    List<CategoryEntity> getCategorySelect();

    CategoryEntity getCategoryByArticleId(Long id);
}
