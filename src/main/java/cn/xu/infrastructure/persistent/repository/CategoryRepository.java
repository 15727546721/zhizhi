package cn.xu.infrastructure.persistent.repository;


import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.repository.ICategoryRepository;
import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import cn.xu.infrastructure.persistent.dao.ICategoryDao;
import cn.xu.infrastructure.persistent.po.ArticleCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class CategoryRepository implements ICategoryRepository {

    @Resource
    private ICategoryDao categoryDao;

    @Override
    public void save(CategoryEntity category) {

        try {
            categoryDao.insert(ArticleCategory.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .description(category.getDescription())
                    .build());
        } catch (Exception e) {
            log.error("保存分类失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存分类失败");
        }
    }

    @Override
    public List<CategoryEntity> queryCategoryList(int page, int size) {
        List<ArticleCategory> articleCategoryList = categoryDao.selectListByPage((page - 1) * size, size);
        log.info("查询分类列表，返回结果：{}", articleCategoryList);
        List<CategoryEntity> categoryEntityList = articleCategoryList.stream()
                .map(this::convertToCategoryEntity)
                .collect(Collectors.toList());

        return categoryEntityList;
    }

    @Override
    public void update(CategoryEntity categoryEntity) {
        try {
            categoryDao.update(ArticleCategory.builder()
                    .id(categoryEntity.getId())
                    .name(categoryEntity.getName())
                    .description(categoryEntity.getDescription())
                    .build());
        } catch (Exception e) {
            log.error("更新分类失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新分类失败");
        }
    }

    @Override
    public void delete(List<Long> idList) {
        try {
            categoryDao.delete(idList);
        } catch (Exception e) {
            log.error("删除分类失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除分类失败");
        }
    }

    @Override
    public List<CategoryEntity> getCategorySelect() {
        List<ArticleCategory> articleCategoryList = categoryDao.selectList();
        log.info("查询分类列表，返回结果：{}", articleCategoryList);
        List<CategoryEntity> categoryEntityList = articleCategoryList.stream()
                .map(this::convertToCategoryEntity)
                .collect(Collectors.toList());

        return categoryEntityList;
    }

    @Override
    public CategoryEntity getCategoryByArticleId(Long id) {
        log.info("查询文章分类，文章ID：{}", id);
        ArticleCategory articleCategory = categoryDao.selectByArticleId(id);
        log.info("查询文章分类，返回结果：{}", articleCategory);
        return convertToCategoryEntity(articleCategory);
    }

    @Override
    public List<CategoryEntity> getCategoryList() {

        List<ArticleCategory> articleCategoryList = categoryDao.selectList();
        log.info("查询分类列表，返回结果：{}", articleCategoryList);

        return articleCategoryList.stream()
                .map(this::convertToCategoryEntity)
                .collect(Collectors.toList());
    }


    private CategoryEntity convertToCategoryEntity(ArticleCategory articleCategory) {
        if (articleCategory == null) {
            return null;
        }
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(articleCategory.getId());
        categoryEntity.setName(articleCategory.getName());
        categoryEntity.setDescription(articleCategory.getDescription());
        categoryEntity.setCreateTime(articleCategory.getCreateTime());
        categoryEntity.setUpdateTime(articleCategory.getUpdateTime());
        return categoryEntity;
    }
}
