package cn.xu.repository.mapper;

import cn.xu.model.entity.Column;
import cn.xu.model.entity.ColumnPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专栏文章关联Mapper接口
 */
@Mapper
public interface ColumnPostMapper {
    
    /**
     * 插入专栏文章关联
     */
    void insert(ColumnPost columnPost);
    
    /**
     * 删除专栏文章关联
     */
    void delete(@Param("columnId") Long columnId, @Param("postId") Long postId);
    
    /**
     * 更新排序值
     */
    void updateSort(@Param("columnId") Long columnId, @Param("postId") Long postId, @Param("sort") Integer sort);
    
    /**
     * 批量更新排序值
     */
    void batchUpdateSort(@Param("columnId") Long columnId, @Param("list") List<ColumnPost> columnPosts);
    
    /**
     * 查询专栏文章关联
     */
    ColumnPost selectByColumnAndPost(@Param("columnId") Long columnId, @Param("postId") Long postId);
    
    /**
     * 查询专栏的所有文章(按排序)
     */
    List<ColumnPost> selectByColumnId(@Param("columnId") Long columnId);
    
    /**
     * 分页查询专栏的文章
     */
    List<ColumnPost> selectByColumnIdWithPage(@Param("columnId") Long columnId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 查询文章所属的专栏
     */
    List<Column> selectColumnsByPostId(@Param("postId") Long postId);
    
    /**
     * 统计专栏的文章数
     */
    int countByColumnId(@Param("columnId") Long columnId);
    
    /**
     * 统计文章所属的专栏数
     */
    int countByPostId(@Param("postId") Long postId);
    
    /**
     * 检查文章是否在专栏中
     */
    int exists(@Param("columnId") Long columnId, @Param("postId") Long postId);
    
    /**
     * 查询上一篇文章(排序值小于当前的最大一个)
     */
    ColumnPost selectPreviousBySort(@Param("columnId") Long columnId, @Param("currentSort") Integer currentSort);
    
    /**
     * 查询下一篇文章(排序值大于当前的最小一个)
     */
    ColumnPost selectNextBySort(@Param("columnId") Long columnId, @Param("currentSort") Integer currentSort);
    
    /**
     * 删除专栏的所有文章关联
     */
    void deleteByColumnId(@Param("columnId") Long columnId);
    
    /**
     * 删除帖子的所有专栏关联
     */
    void deleteByPostId(@Param("postId") Long postId);
}
