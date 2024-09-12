package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.repository.ICategoryRepository;
import cn.xu.infrastructure.persistent.dao.ICategoryDao;
import cn.xu.infrastructure.persistent.po.Category;
import cn.xu.types.common.Constants;
import cn.xu.types.exception.AppException;
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
            categoryDao.insert(Category.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .description(category.getDescription())
                    .build());
        } catch (Exception e) {
            log.error("保存分类失败", e);
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "保存分类失败");
        }
    }

    @Override
    public List<CategoryEntity> queryCategoryList(int page, int size) {
        List<Category> categoryList = categoryDao.selectListByPage((page - 1) * size, size);
        log.info("查询分类列表，返回结果：{}", categoryList);
        List<CategoryEntity> categoryEntityList = categoryList.stream()
                .map(this::convertToCategoryEntity)
                .collect(Collectors.toList());

        return categoryEntityList;
    }

    @Override
    public void update(CategoryEntity categoryEntity) {
        try {
            categoryDao.update(Category.builder()
                    .id(categoryEntity.getId())
                    .name(categoryEntity.getName())
                    .description(categoryEntity.getDescription())
                    .build());
        } catch (Exception e) {
            log.error("更新分类失败", e);
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "更新分类失败");
        }
    }

    @Override
    public void delete(List<Long> idList) {
        try {
            categoryDao.delete(idList);
        } catch (Exception e) {
            log.error("删除分类失败", e);
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "删除分类失败");
        }
    }

    private CategoryEntity convertToCategoryEntity(Category category) {
        if (category == null) {
            return null;
        }
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(category.getId());
        categoryEntity.setName(category.getName());
        categoryEntity.setDescription(category.getDescription());
        categoryEntity.setCreateTime(category.getCreateTime());
        categoryEntity.setUpdateTime(category.getUpdateTime());
        return categoryEntity;
    }
}
