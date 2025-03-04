package cn.xu.domain.article.service.impl;

import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.repository.ICategoryRepository;
import cn.xu.domain.article.service.ICategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CategoryService implements ICategoryService {

    @Resource
    private ICategoryRepository categoryRepository;

    @Override
    public List<CategoryEntity> queryCategoryList(int page, int size) {
        List<CategoryEntity> categoryEntity = categoryRepository.queryCategoryList(page, size);
        return categoryEntity;
    }

    @Override
    public void save(CategoryEntity category) {
        categoryRepository.save(category);
    }

    @Override
    public void update(CategoryEntity categoryEntity) {
        categoryRepository.update(categoryEntity);
    }

    @Override
    public void delete(List<Long> idList) {
        categoryRepository.delete(idList);
    }

    @Override
    public List<CategoryEntity> getCategorySelect() {
        return categoryRepository.getCategorySelect();
    }

    @Override
    public CategoryEntity getCategoryByArticleId(Long id) {
        return categoryRepository.getCategoryByArticleId(id);
    }

    @Override
    public List<CategoryEntity> getCategoryList() {
        return categoryRepository.getCategoryList();
    }
}
