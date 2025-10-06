package cn.xu.domain.post.repository;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.service.IPostTagService;

import java.util.List;

/**
 * 帖子标签仓储接口
 * 负责帖子与标签关联关系的数据访问
 */
public interface IPostTagRepository {
    
    /**
     * 保存帖子标签关联关系
     *
     * @param postId 帖子ID
     * @param tagIds 标签ID列表
     */
    void savePostTag(Long postId, List<Long> tagIds);
    
    /**
     * 根据帖子ID获取标签ID列表
     *
     * @param postId 帖子ID
     * @return 标签ID列表
     */
    List<Long> getTagIdsByPostId(Long postId);
    
    /**
     * 根据标签ID获取帖子列表
     *
     * @param tagId 标签ID
     * @param offset 偏移量
     * @param limit 数量
     * @return 帖子列表
     */
    List<PostEntity> getPostsByTagId(Long tagId, int offset, int limit);
    
    /**
     * 批量获取帖子的标签ID列表
     *
     * @param postIds 帖子ID列表
     * @return 帖子与标签的关联关系列表
     */
    List<IPostTagService.PostTagRelation> batchGetTagIdsByPostIds(List<Long> postIds);
    
    /**
     * 删除帖子的所有标签关联关系
     *
     * @param postId 帖子ID
     */
    void deletePostTags(Long postId);
    
    /**
     * 根据帖子类型获取标签统计信息
     *
     * @param postType 帖子类型
     * @param limit 限制数量
     * @return 标签使用统计列表
     */
    List<IPostTagService.TagStatistics> getTagStatisticsByPostType(PostType postType, int limit);
    
    /**
     * 获取热门标签列表
     *
     * @param limit 限制数量
     * @return 热门标签列表
     */
    List<IPostTagService.TagStatistics> getHotTags(int limit);
    
    /**
     * 获取推荐标签列表
     *
     * @return 推荐标签列表
     */
    List<IPostTagService.TagStatistics> getRecommendedTags();
}