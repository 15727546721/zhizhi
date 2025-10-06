package cn.xu.domain.post.service.impl;

import cn.xu.domain.post.model.entity.CategoryEntity;
import cn.xu.domain.post.repository.ICategoryRepository;
import cn.xu.domain.post.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现类
 * 负责分类相关的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {
    
    private final ICategoryRepository categoryRepository;
    
    @Override
    public void save(CategoryEntity category) {
        categoryRepository.save(category);
    }
    
    @Override
    public List<CategoryEntity> queryCategoryList(int page, int size) {
        return categoryRepository.queryCategoryList(page, size);
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
    public CategoryEntity getCategoryByPostId(Long id) {
        return categoryRepository.getCategoryByPostId(id);
    }
    
    @Override
    public List<CategoryEntity> getCategoryList() {
        return categoryRepository.getCategoryList();
    }
    
    @Override
    public List<CategoryEntity> searchCategories(String keyword) {
        try {
            return categoryRepository.searchCategories(keyword);
        } catch (Exception e) {
            log.error("搜索分类失败, keyword: {}", keyword, e);
            return null;
        }
    }
}