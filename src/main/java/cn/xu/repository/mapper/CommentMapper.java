package cn.xu.repository.mapper;

import cn.xu.model.enums.CommentSortType;
import cn.xu.model.dto.comment.CommentCountResult;
import cn.xu.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论Mapper接口
 */
@Mapper
public interface CommentMapper {
    /**
     * 插入评论
     */
    Long saveComment(Comment comment);
    
    /**
     * 保存评论图片
     * @param commentId 评论ID
     * @param imageUrl 图片URL
     * @param sortOrder 排序序号
     */
    void saveImages(@Param("commentId") Long commentId, @Param("imageUrl") String imageUrl, @Param("sortOrder") int sortOrder);

    /**
     * 根据ID删除评论
     *
     * @param id 评论ID
     */
    void deleteById(@Param("id") Long id);

    /**
     * 根据ID查询评论
     *
     * @param id 评论ID
     * @return 评论实体
     */
    Comment selectById(@Param("id") Long id);

    // 按热度查询一级评论（带图片）
    List<Comment> findRootCommentsByHot(
            @Param("targetType") int targetType,
            @Param("targetId") long targetId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    // 按时间查询一级评论（带图片）
    List<Comment> findRootCommentsByTime(
            @Param("targetType") int targetType,
            @Param("targetId") long targetId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    List<Comment> findRepliesByParentIdsByTime(@Param("parentIds") List<Long> parentIds,
                                               @Param("size") int size);

    List<Comment> findRepliesByParentIdsByHot(@Param("parentIds") List<Long> parentIds,
                                              @Param("size") int size);


    // 预览回复查询（带图片）
    List<Comment> selectPreviewRepliesByParentIds(
            @Param("parentIds") List<Long> parentIds,
            @Param("previewSize") int previewSize
    );

    /**
     * 批量删除评论
     *
     * @param commentIds 评论ID列表
     * @return 删除的记录数
     */
    int batchDelete(@Param("commentIds") List<Long> commentIds);

    /**
     * 根据父评论ID查询子评论列表
     *
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    List<Comment> findByParentId(@Param("parentId") Long parentId);

    /**
     * 根据父评论ID列表批量查询子评论
     *
     * @param parentIds 父评论ID列表
     * @return 子评论列表
     */
    List<Comment> findByParentIds(@Param("parentIds") List<Long> parentIds);

    /**
     * 分页查询二级评论列表
     *
     * @param parentId 父评论ID
     * @param offset   偏移量
     * @param limit    每页数量
     * @return 二级评论列表
     */
    List<Comment> findRepliesByPage(@Param("parentId") Long parentId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 分页查询一级评论列表（前台用，支持多条件过滤）
     */
    List<Comment> findRootCommentsByPage(@Param("targetType") Integer targetType,
                                         @Param("targetId") Long targetId,
                                         @Param("userId") Long userId,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);
    
    /**
     * 统计一级评论数（前台用，支持多条件过滤）
     */
    Long countRootComments(@Param("targetType") Integer targetType,
                           @Param("targetId") Long targetId,
                           @Param("userId") Long userId);

    /**
     * 根据父评论ID删除所有子评论
     *
     * @param parentId 父评论ID
     */
    int deleteByParentId(@Param("parentId") Long parentId);

    /**
     * 更新评论点赞数
     *
     * @param commentId 评论ID
     * @param count 增量值（正数表示增加，负数表示减少）
     */
    void updateLikeCount(@Param("commentId") Long commentId, @Param("count") Integer count);

    /**
     * 更新评论回复数
     */
    void updateCommentCount(@Param("commentId") Long commentId, @Param("count") int count);

    /**
     * 根据条件查询二级评论列表
     */
    List<Comment> findReplyListByPage(@Param("parentIds") List<Long> parentIds,
                                      @Param("sortType") CommentSortType sortType,
                                      @Param("pageNo") Integer pageNo,
                                      @Param("pageSize") Integer pageSize);


    /**
     * 分批次查询评论（用于大数据量ES初始化）
     * @param offset 偏移量
     * @param batchSize 每批大小
     * @return 评论批次
     */
    List<Comment> findCommentsBatch(int offset, int batchSize);

    List<Comment> findRepliesByParentIdByHot(@Param("parentId") Long parentId, @Param("offset") int offset, @Param("size") int size);

    List<Comment> findRepliesByParentIdByTime(@Param("parentId") Long parentId, @Param("offset") int offset, @Param("size") int size);

    List<Comment> selectCommentsByIds(@Param("commentIdList") List<Long> commentIdList);
    
    /**
     * 更新评论
     */
    void updateComment(Comment comment);
    
    /**
     * 根据目标类型和目标ID统计评论数
     */
    Long countByTargetTypeAndTargetId(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);
    
    /**
     * 批量统计目标的评论数
     */
    List<CommentCountResult> batchCountByTargetIds(@Param("targetType") Integer targetType, @Param("targetIds") List<Long> targetIds);
    
    /**
     * 统计用户的评论数
     */
    Long countByUserId(@Param("userId") Long userId);
    
    /**
     * 统计父评论的子评论数
     */
    Long countByParentId(@Param("parentId") Long parentId);
    
    /**
     * 根据用户ID查询评论列表（分页）
     */
    List<Comment> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 增加回复数（原子操作）
     */
    int incrementReplyCount(@Param("commentId") Long commentId);
    
    /**
     * 减少回复数（原子操作）
     */
    int decrementReplyCount(@Param("commentId") Long commentId);
    
    /**
     * 统计所有评论数
     */
    Long countAll();
    
    /**
     * 查询所有根评论（管理后台用）
     */
    List<Comment> findAllRootComments(@Param("targetType") Integer targetType, 
                                       @Param("targetId") Long targetId, 
                                       @Param("offset") int offset, 
                                       @Param("limit") int limit);
    
    /**
     * 统计根评论数量（管理后台用）
     */
    Long countAllRootComments(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);
    
    /**
     * 获取评论点赞数
     *
     * @param commentId 评论ID
     * @return 点赞数
     */
    Integer getLikeCount(@Param("commentId") Long commentId);
    
    /**
     * 获取评论作者ID
     *
     * @param commentId 评论ID
     * @return 作者ID
     */
    Long getAuthorId(@Param("commentId") Long commentId);

    // ==================== 统计相关方法 ====================
    
    /**
     * 统计指定时间之后创建的评论数
     */
    Long countByCreateTimeAfter(@Param("createTime") java.time.LocalDateTime createTime);
    
    /**
     * 统计指定时间范围内创建的评论数
     */
    Long countByCreateTimeBetween(@Param("startTime") java.time.LocalDateTime startTime, 
                                   @Param("endTime") java.time.LocalDateTime endTime);
}
