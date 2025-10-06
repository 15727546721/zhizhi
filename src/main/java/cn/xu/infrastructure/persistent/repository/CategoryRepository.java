package cn.xu.infrastructure.persistent.repository;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.post.model.entity.CategoryEntity;
import cn.xu.domain.post.repository.ICategoryRepository;
import cn.xu.infrastructure.persistent.converter.CategoryConverter;
import cn.xu.infrastructure.persistent.dao.CategoryMapper;
import cn.xu.infrastructure.persistent.po.PostCategory;
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
            PostCategory postCategory = categoryConverter.toDataObject(category);
            categoryDao.insert(postCategory);
            category.setId(postCategory.getId());
        } catch (Exception e) {
            log.error("保存分类失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存分类失败");
        }
    }

    @Override
    public List<CategoryEntity> queryCategoryList(int page, int size) {
        List<PostCategory> postCategoryList = categoryDao.selectListByPage((page - 1) * size, size);
        log.info("查询分类列表，返回结果：{}", postCategoryList);
        return categoryConverter.toDomainEntities(postCategoryList);
    }

    @Override
    public void update(CategoryEntity categoryEntity) {
        try {
            PostCategory postCategory = categoryConverter.toDataObject(categoryEntity);
            categoryDao.update(postCategory);
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
        List<PostCategory> postCategoryList = categoryDao.selectList();
        log.info("查询分类列表，返回结果：{}", postCategoryList);
        return categoryConverter.toDomainEntities(postCategoryList);
    }

    @Override
    public CategoryEntity getCategoryByPostId(Long id) {
        log.info("查询帖子分类，帖子ID：{}", id);
        PostCategory postCategory = categoryDao.selectByPostId(id);
        log.info("查询帖子分类，返回结果：{}", postCategory);
        return categoryConverter.toDomainEntity(postCategory);
    }

    @Override
    public List<CategoryEntity> getCategoryList() {
        List<PostCategory> postCategoryList = categoryDao.selectList();
        log.info("查询分类列表，返回结果：{}", postCategoryList);
        return categoryConverter.toDomainEntities(postCategoryList);
    }
    
    @Override
    public List<CategoryEntity> searchCategories(String keyword) {
        List<PostCategory> postCategoryList = categoryDao.searchCategories(keyword);
        log.info("搜索分类列表，关键词：{}，返回结果：{}", keyword, postCategoryList);
        return categoryConverter.toDomainEntities(postCategoryList);
    }
}