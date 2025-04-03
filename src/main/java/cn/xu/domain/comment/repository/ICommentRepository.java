package cn.xu.domain.comment.repository;

import cn.xu.api.web.model.dto.comment.CommentQueryRequest;
import cn.xu.api.web.model.vo.comment.CommentPageVO;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentSortType;

import java.util.List;

public interface ICommentRepository {
    /**
     * 保存评论
     *
     * @param commentEntity 评论实体
     * @return 评论ID
     */
    Long save(CommentEntity commentEntity);

    /**
     * 添加评论
     */
    Long addComment(CommentEntity commentEntity);

    /**
     * 回复评论
     */
    Long replyComment(CommentEntity commentEntity);

    /**
     * 获取文章评论列表
     */
    List<CommentEntity> getArticleComments(Long articleId);

    /**
     * 获取话题评论列表
     */
    List<CommentEntity> getTopicComments(Long topicId);

    /**
     * 根据ID获取评论
     *
     * @param id 评论ID
     * @return 评论实体
     */
    CommentEntity findById(Long id);

    /**
     * 根据ID删除评论
     *
     * @param id 评论ID
     */
    void deleteById(Long id);

    /**
     * 根据类型和目标ID查询评论列表
     */
    List<CommentEntity> findByTypeAndTargetId(Integer type, Long targetId);

    /**
     * 根据父评论ID查询子评论列表
     *
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    List<CommentEntity> findByParentId(Long parentId);

    /**
     * 批量删除评论
     *
     * @param commentIds 评论ID列表
     */
    void batchDelete(List<Long> commentIds);

    /**
     * 查询一级评论列表
     */
    List<CommentEntity> findRootComments(Long targetId, Integer type);

    /**
     * 分页查询二级评论列表
     */
    List<CommentEntity> findRepliesByPage(Long parentId, int offset, int limit);

    /**
     * 分页查询一级评论列表
     *
     * @param type   评论类型（可选）
     * @param userId 用户ID（可选）
     * @param offset 偏移量
     * @param limit  每页数量
     * @return 一级评论列表
     */
    List<CommentEntity> findRootCommentsByPage(Integer type, Long userId, int offset, int limit);

    /**
     * 统计一级评论总数
     *
     * @param type   评论类型（可选）
     * @param userId 用户ID（可选）
     * @return 评论总数
     */
    long countRootComments(Integer type, Long userId);

    /**
     * 根据父评论ID删除所有子评论
     *
     * @param parentId 父评论ID
     */
    int deleteByParentId(Long parentId);

    /**
     * 根据父评论ID列表批量查询子评论
     *
     * @param parentIds 父评论ID列表
     * @return 子评论列表
     */
    List<CommentEntity> findRepliesByParentIds(List<Long> parentIds);

    /**
     * 根据评论ID列表查询评论
     *
     * @param request
     * @return
     */
    List<CommentEntity> findRootCommentList(CommentQueryRequest request);

    /**
     * 根据评论ID列表分页查询子评论
     */
    List<CommentEntity> findReplyListByPage(List<Long> parentIds, CommentSortType sortType, Integer page, Integer size);

    /**
     * 根据评论ID查询评论及用户信息
     * @param commentId
     * @return
     */
    CommentEntity findCommentWithUserById(Long commentId);
}
