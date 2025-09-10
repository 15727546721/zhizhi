package cn.xu.infrastructure.persistent.repository;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.repository.ICategoryRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.converter.CategoryConverter;
import cn.xu.infrastructure.persistent.dao.CategoryMapper;
import cn.xu.infrastructure.persistent.po.ArticleCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 分类仓储实现类
 * 通过CategoryConverter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CategoryRepository implements ICategoryRepository {

    private final CategoryMapper categoryDao;
    private final CategoryConverter categoryConverter;

    @Override
    public void save(CategoryEntity category) {
        try {
            ArticleCategory articleCategory = categoryConverter.toDataObject(category);
            categoryDao.insert(articleCategory);
            category.setId(articleCategory.getId());
        } catch (Exception e) {
            log.error("保存分类失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存分类失败");
        }
    }

    @Override
    public List<CategoryEntity> queryCategoryList(int page, int size) {
        List<ArticleCategory> articleCategoryList = categoryDao.selectListByPage((page - 1) * size, size);
        log.info("查询分类列表，返回结果：{}", articleCategoryList);
        return categoryConverter.toDomainEntities(articleCategoryList);
    }

    @Override
    public void update(CategoryEntity categoryEntity) {
        try {
            ArticleCategory articleCategory = categoryConverter.toDataObject(categoryEntity);
            categoryDao.update(articleCategory);
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
        return categoryConverter.toDomainEntities(articleCategoryList);
    }

    @Override
    public CategoryEntity getCategoryByArticleId(Long id) {
        log.info("查询文章分类，文章ID：{}", id);
        ArticleCategory articleCategory = categoryDao.selectByArticleId(id);
        log.info("查询文章分类，返回结果：{}", articleCategory);
        return categoryConverter.toDomainEntity(articleCategory);
    }

    @Override
    public List<CategoryEntity> getCategoryList() {
        List<ArticleCategory> articleCategoryList = categoryDao.selectList();
        log.info("查询分类列表，返回结果：{}", articleCategoryList);
        return categoryConverter.toDomainEntities(articleCategoryList);
    }



}
