package cn.xu.repository;

import cn.xu.model.entity.Column;
import cn.xu.model.entity.ColumnPost;

import java.util.List;

/**
 * 专栏文章关联仓储接口
 */
public interface ColumnPostRepository {
    
    /**
     * 保存专栏文章关联
     */
    void save(ColumnPost columnPost);
    
    /**
     * 删除专栏文章关联
     */
    void delete(Long columnId, Long postId);
    
    /**
     * 更新排序值
     */
    void updateSort(Long columnId, Long postId, Integer sort);
    
    /**
     * 批量更新排序值
     */
    void batchUpdateSort(Long columnId, List<ColumnPost> columnPosts);
    
    /**
     * 查询专栏文章关联
     */
    ColumnPost findByColumnAndPost(Long columnId, Long postId);
    
    /**
     * 查询专栏的所有文章(按排序)
     */
    List<ColumnPost> findByColumnId(Long columnId);
    
    /**
     * 分页查询专栏的文章
     */
    List<ColumnPost> findByColumnIdWithPage(Long columnId, int offset, int limit);
    
    /**
     * 查询文章所属的专栏
     */
    List<Column> findColumnsByPostId(Long postId);
    
    /**
     * 统计专栏的文章数
     */
    int countByColumnId(Long columnId);
    
    /**
     * 统计文章所属的专栏数
     */
    int countByPostId(Long postId);
    
    /**
     * 检查文章是否在专栏中
     */
    boolean exists(Long columnId, Long postId);
    
    /**
     * 查询上一篇文章(排序值小于当前的最大一个)
     */
    ColumnPost findPreviousBySort(Long columnId, Integer currentSort);
    
    /**
     * 查询下一篇文章(排序值大于当前的最小一个)
     */
    ColumnPost findNextBySort(Long columnId, Integer currentSort);
    
    /**
     * 删除专栏的所有文章关联
     */
    void deleteByColumnId(Long columnId);
    
    /**
     * 删除帖子的所有专栏关联
     */
    void deleteByPostId(Long postId);
}
