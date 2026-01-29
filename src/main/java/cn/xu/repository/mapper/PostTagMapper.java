package cn.xu.repository.mapper;

import cn.xu.model.dto.post.TagStatistics;
import cn.xu.model.entity.PostTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子标签Mapper接口
 * <p>处理帖子标签关联的数据库操作</p>
 
 */
@Mapper
public interface PostTagMapper {
    void insert(PostTag postTag);

    void insertTags(List<PostTag> postTags);

    void deleteByPostIds(@Param("postIds") List<Long> postIds);

    void insertBatchByList(List<PostTag> postTags);

    void deleteByPostId(@Param("postId") Long postId);
    
    // 根据帖子ID和标签ID列表删除标签关联
    void deleteByPostIdAndTagIds(@Param("postId") Long postId, @Param("tagIds") List<Long> tagIds);
    
    // 根据标签ID删除所有关联关系
    void deleteByTagId(@Param("tagId") Long tagId);
    
    // 根据帖子ID查询标签ID
    List<Long> selectTagIdsByPostId(@Param("postId") Long postId);
    
    // 根据标签ID查询帖子ID列表
    List<Long> selectPostIdsByTagId(@Param("tagId") Long tagId, @Param("offset") int offset, @Param("limit") int limit);
    
    // 根据帖子ID列表查询标签关联信息
    List<PostTag> selectByPostIds(@Param("postIds") List<Long> postIds);
    
    // 统计标签使用次数
    int countByTagId(@Param("tagId") Long tagId);
    
    // 获取热门标签统计
    List<TagStatistics> selectHotTags(@Param("limit") int limit);
}
