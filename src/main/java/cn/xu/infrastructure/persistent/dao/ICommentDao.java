package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Comment;
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
     */
    int deleteById(@Param("id") Long id);

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
}
