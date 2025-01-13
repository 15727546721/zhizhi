package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Comment;
import cn.xu.domain.comment.model.entity.CommentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ICommentDao {
    /**
     * 插入评论
     */
    int insert(Comment comment);

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
     * 统计一级评论总数
     *
     * @param type   评论类型（可选）
     * @param userId 用户ID（可选）
     * @return 评论总数
     */
    long countRootComments(@Param("type") Integer type, @Param("userId") Long userId);

    /**
     * 分页查询一级评论列表
     *
     * @param type   评论类型（可选）
     * @param userId 用户ID（可选）
     * @param offset 偏移量
     * @param limit  每页数量
     * @return 一级评论列表
     */
    List<Comment> findRootCommentsByPage(@Param("type") Integer type, @Param("userId") Long userId,
                                       @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 根据ID查询评论
     *
     * @param id 评论ID
     * @return 评论实体
     */
    CommentEntity selectById(@Param("id") Long id);

    /**
     * 根据父评论ID删除所有子评论
     *
     * @param parentId 父评论ID
     */
    void deleteByParentId(@Param("parentId") Long parentId);
}
