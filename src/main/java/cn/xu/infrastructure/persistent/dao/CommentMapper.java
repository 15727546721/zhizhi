package cn.xu.infrastructure.persistent.dao;

import cn.xu.api.web.model.dto.comment.CommentQueryRequest;
import cn.xu.api.web.model.vo.comment.FindCommentItemVO;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentSortType;
import cn.xu.infrastructure.persistent.po.Comment;
import cn.xu.infrastructure.persistent.po.CommentImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    /**
     * 插入评论
     */
    Long saveComment(Comment comment);

    /**
     * 插入评论图片
     *
     * @param images
     * @return
     */
    Long saveImages(List<CommentImage> images);

    /**
     * 根据ID删除评论
     *
     * @param id 评论ID
     */
    void deleteById(@Param("id") Long id);

    /**
     * 根据ID查询评论
     */
    Comment findById(@Param("id") Long id);

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
    List<CommentEntity> selectPreviewRepliesByParentIds(
            @Param("parentIds") List<Long> parentIds,
            @Param("previewSize") int previewSize
    );

    /**
     * 根据类型和目标ID查询评论列表
     *
     * @param type     评论类型（1-文章；2-话题）
     * @param targetId 目标ID
     * @return 评论列表
     */
    List<Comment> findByTypeAndTargetId(@Param("type") Integer type, @Param("targetId") Long targetId);

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
     * 根据类型和目标ID删除评论
     *
     * @param type     评论类型（1-文章；2-话题）
     * @param targetId 目标ID
     */
    void deleteByTypeAndTargetId(@Param("type") int type, @Param("targetId") Long targetId);

    /**
     * 查询一级评论列表
     *
     * @param targetId 目标ID
     * @param type     评论类型（可选）
     * @return 一级评论列表
     */
    List<Comment> findRootComments(@Param("targetId") Long targetId, @Param("type") Integer type);

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
     * 分页查询一级评论列表
     */
    List<Comment> findRootCommentsByPage(@Param("targetType") Integer targetType,
                                         @Param("targetId") Long targetId,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    /**
     * 根据ID查询评论
     *
     * @param id 评论ID
     * @return 评论实体
     */
    Comment selectById(@Param("id") Long id);

    /**
     * 根据父评论ID删除所有子评论
     *
     * @param parentId 父评论ID
     */
    int deleteByParentId(@Param("parentId") Long parentId);

    /**
     * 更新评论点赞数
     *
     * @param targetId
     * @param count
     */
    void updateLikeCount(@Param("targetId") long targetId, @Param("count") Integer count);

    /**
     * 更新评论回复数
     *
     * @param commentId
     * @param count
     */
    void updateCommentCount(@Param("commentId") Long commentId, @Param("count") int count);

    /**
     * 根据条件查询一级评论列表
     *
     * @param request
     * @return
     */
    List<Comment> findRootCommentList(CommentQueryRequest request);

    /**
     * 根据条件查询二级评论列表
     */
    List<Comment> findReplyListByPage(@Param("parentIds") List<Long> parentIds,
                                      @Param("sortType") CommentSortType sortType,
                                      @Param("pageNo") Integer pageNo,
                                      @Param("pageSize") Integer pageSize);

    /**
     * 根据评论ID查询评论及用户信息
     *
     * @param commentId
     * @return
     */
    CommentEntity findCommentWithUserById(@Param("id") Long commentId);

    /**
     * 根据评论ID查询评论及用户信息
     *
     * @param targetType
     * @param targetId
     * @param offset
     * @param size
     * @return
     */
    List<FindCommentItemVO> findRootCommentWithUser(@Param("targetType") Integer targetType,
                                                    @Param("targetId") Long targetId,
                                                    @Param("offset") int offset,
                                                    @Param("size") int size);


    /**
     * 分批次查询评论（用于大数据量ES初始化）
     * @param offset 偏移量
     * @param batchSize 每批大小
     * @return 评论批次
     */
    List<CommentEntity> findCommentsBatch(int offset, int batchSize);

    Long countByParentId(Long commentId);

    List<Comment> findRepliesByParentIdByHot(@Param("parentId") Long parentId, @Param("offset") int offset, @Param("size") int size);

    List<Comment> findRepliesByParentIdByTime(@Param("parentId") Long parentId, @Param("offset") int offset, @Param("size") int size);

    List<Comment> selectCommentsByIds(@Param("commentIdList") List<Long> commentIdList);
}
