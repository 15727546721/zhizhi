package cn.xu.domain.post.service.impl;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.repository.IPostCategoryRepository;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.domain.post.service.IPostCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 帖子分类服务实现类
 * 专门处理帖子与分类的关联关系
 */
@Slf4j
@Service
public class PostCategoryServiceImpl implements IPostCategoryService {
    
    @Resource
    private IPostRepository postRepository;
    
    @Resource
    private IPostCategoryRepository postCategoryRepository;
    
    @Override
    public List<PostEntity> getPostsByCategoryId(Long categoryId, Integer pageNo, Integer pageSize) {
        // 计算偏移量
        int offset = (pageNo - 1) * pageSize;
        // 调用仓储层获取帖子列表
        return postCategoryRepository.getPostsByCategoryId(categoryId, offset, pageSize);
    }
    
    @Override
    public List<CategoryInfo> getHotCategoriesByType(PostType postType, int limit) {
        // 调用仓储层获取热门分类列表
        return postCategoryRepository.getHotCategoriesByType(postType, limit);
    }
    
    @Override
    public List<CategoryInfo> getAllCategories() {
        // 调用仓储层获取所有分类列表
        return postCategoryRepository.getAllCategories();
    }
    
    @Override
    public CategoryInfo getCategoryByName(String categoryName) {
        // 调用仓储层根据分类名称获取分类信息
        return postCategoryRepository.getCategoryByName(categoryName);
    }
    
    @Override
    public List<CategoryInfo> getRecommendedCategories() {
        // 调用仓储层获取推荐分类列表
        return postCategoryRepository.getRecommendedCategories();
    }
}