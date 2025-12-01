package cn.xu.repository;

import cn.xu.model.entity.Comment;

import java.util.List;
import java.util.Map;

/**
 * 评论仓储接口
 *
 * @author xu
 */
public interface ICommentRepository {
    
    // ========== 基础CRUD ==========
    
    Long save(Comment comment);
    
    void update(Comment comment);
    
    void deleteById(Long id);
    
    void batchDelete(List<Long> commentIds);
    
    int deleteByParentId(Long parentId);
    
    Comment findById(Long id);
    
    List<Comment> findByIds(List<Long> ids);
    
    // ========== 根评论查询 ==========
    
    List<Comment> findRootCommentsByHot(int targetType, long targetId, int pageNo, int pageSize);
    
    List<Comment> findRootCommentsByTime(int targetType, long targetId, int pageNo, int pageSize);
    
    List<Comment> findRootComments(Integer type, Long targetId, Long userId, int offset, int limit);
    
    Long countRootComments(Integer type, Long targetId, Long userId);
    
    // ========== 子评论查询 ==========
    
    List<Comment> findByParentId(Long parentId);
    
    List<Comment> findByParentIds(List<Long> parentIds);
    
    List<Comment> findRepliesByPage(Long parentId, int offset, int limit);
    
    List<Comment> findRepliesByParentIdsByHot(List<Long> parentIds, int size);
    
    List<Comment> findRepliesByParentIdsByTime(List<Long> parentIds, int size);
    
    List<Comment> findRepliesByParentIdByHot(Long parentId, int page, int size);
    
    List<Comment> findRepliesByParentIdByTime(Long parentId, int page, int size);
    
    // ========== 统计查询 ==========
    
    Long countByTargetTypeAndTargetId(Integer targetType, Long targetId);
    
    Map<Long, Long> batchCountByTargetIds(Integer targetType, List<Long> targetIds);
    
    Long countByUserId(Long userId);
    
    // ========== 用户评论查询 ==========
    
    List<Comment> findByUserId(Long userId, int offset, int limit);
    
    // ========== 批量查询（ES初始化用） ==========
    
    List<Comment> findCommentBatch(int offset, int batchSize);
    
    // ========== 回复数更新（原子操作） ==========
    
    void incrementReplyCount(Long commentId);
    
    void decrementReplyCount(Long commentId);
    
    // ========== 评论图片 ==========
    
    /**
     * 保存评论图片
     * @param commentId 评论ID
     * @param imageUrls 图片URL列表
     */
    void saveImages(Long commentId, List<String> imageUrls);
}
