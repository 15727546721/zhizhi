package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.repository.IPostCategoryRepository;
import cn.xu.domain.post.service.IPostCategoryService;
import cn.xu.infrastructure.persistent.converter.PostConverter;
import cn.xu.infrastructure.persistent.dao.PostCategoryMapper;
import cn.xu.infrastructure.persistent.dao.PostMapper;
import cn.xu.infrastructure.persistent.po.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子分类仓储实现类
 * 负责帖子与分类关联关系的数据访问
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PostCategoryRepository implements IPostCategoryRepository {
    
    @Resource
    private final PostCategoryMapper postCategoryMapper;
    
    @Resource
    private final PostMapper postMapper;
    
    @Resource
    private final PostConverter postConverter;
    
    @Override
    public List<PostEntity> getPostsByCategoryId(Long categoryId, int offset, int limit) {
        // 根据分类ID获取帖子ID列表
        List<Long> postIds = postCategoryMapper.selectPostIdsByCategoryId(categoryId, offset, limit);
        
        // 根据帖子ID列表获取帖子详情
        if (postIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 逐个获取帖子详情
        List<Post> posts = new ArrayList<>();
        for (Long postId : postIds) {
            Post post = postMapper.findById(postId);
            if (post != null) {
                posts.add(post);
            }
        }
        return posts.stream()
                .map(post -> postConverter.toDomainEntity(post))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<IPostCategoryService.CategoryInfo> getHotCategoriesByType(PostType postType, int limit) {
        // 获取热门分类列表
        List<PostCategoryMapper.CategoryStatistics> hotCategories = postCategoryMapper.selectHotCategories(postType.getCode(), limit);
        
        // 转换为CategoryInfo对象
        return hotCategories.stream()
                .map(stat -> new IPostCategoryService.CategoryInfo(
                        stat.getCategoryId(),
                        stat.getCategoryName(),
                        stat.getCategoryDescription(),
                        stat.getPostCount().longValue(),
                        postType))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<IPostCategoryService.CategoryInfo> getAllCategories() {
        // 获取所有分类列表
        List<cn.xu.infrastructure.persistent.po.PostCategory> categories = postCategoryMapper.selectAllCategories();
        
        // 转换为CategoryInfo对象
        return categories.stream()
                .map(category -> new IPostCategoryService.CategoryInfo(
                        category.getId(),
                        category.getName(),
                        category.getDescription(),
                        0L, // 这里暂时设置为0，实际应该查询该分类下的帖子数量
                        null)) // 这里暂时设置为null，实际应该根据业务确定主要支持的帖子类型
                .collect(Collectors.toList());
    }
    
    @Override
    public IPostCategoryService.CategoryInfo getCategoryByName(String categoryName) {
        // 根据分类名称获取分类信息
        cn.xu.infrastructure.persistent.po.PostCategory category = postCategoryMapper.selectByCategoryName(categoryName);
        
        if (category == null) {
            return null;
        }
        
        // 转换为CategoryInfo对象
        return new IPostCategoryService.CategoryInfo(
                category.getId(),
                category.getName(),
                category.getDescription(),
                0L, // 这里暂时设置为0，实际应该查询该分类下的帖子数量
                null); // 这里暂时设置为null，实际应该根据业务确定主要支持的帖子类型
    }
    
    @Override
    public List<IPostCategoryService.CategoryInfo> getRecommendedCategories() {
        // 获取推荐分类列表（这里简单返回所有分类，实际可以根据业务逻辑进行推荐）
        return getAllCategories();
    }
}