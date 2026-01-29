package cn.xu.repository;

import cn.xu.model.dto.post.PostTagRelation;
import cn.xu.model.dto.post.TagStatistics;
import cn.xu.model.entity.Post;

import java.util.List;

/**
 * 帖子标签仓储接口
 */
public interface PostTagRepository {
    
    /**
     * 保存帖子标签关联关系
     */
    void savePostTag(Long postId, List<Long> tagIds);
    
    /**
     * 根据帖子ID获取标签ID列表
     */
    List<Long> getTagIdsByPostId(Long postId);
    
    /**
     * 根据标签ID获取帖子列表
     */
    List<Post> getPostsByTagId(Long tagId, int offset, int limit);
    
    /**
     * 批量获取帖子的标签ID列表
     */
    List<PostTagRelation> batchGetTagIdsByPostIds(List<Long> postIds);
    
    /**
     * 删除帖子的所有标签关联关系
     */
    void deletePostTags(Long postId);
    
    /**
     * 获取热门标签列表
     */
    List<TagStatistics> getHotTags(int limit);
    
    /**
     * 获取推荐标签列表
     */
    List<TagStatistics> getRecommendedTags();
}
