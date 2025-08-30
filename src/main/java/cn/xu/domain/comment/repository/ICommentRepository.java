package cn.xu.domain.comment.repository;

import cn.xu.api.web.model.dto.comment.FindChildCommentItemVO;
import cn.xu.api.web.model.vo.comment.FindCommentItemVO;
import cn.xu.domain.comment.model.entity.CommentEntity;

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
     * 查询所有评论（用于ES数据初始化）
     * @return 所有评论列表
     */
    List<CommentEntity> findCommentBatch(int offset, int batchSize);

    /**
     * 根据ID获取评论
     *
     * @param id 评论ID
     * @return 评论实体
     */
    CommentEntity findById(Long id);

    List<CommentEntity> findRootCommentsByHot(int targetType, long targetId, int offset, int pageSize);

    List<CommentEntity> findRootCommentsByTime(int targetType, long targetId, int offset, int pageSize);

    /**
     * 根据ID删除评论
     *
     * @param id 评论ID
     */
    void deleteById(Long id);

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
     * 查询一级评论的分页列表
     * @param type 评论目标类型
     * @param targetId 目标ID
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 一级评论实体列表
     */
    List<CommentEntity> findRootComments(Integer type, Long targetId, int offset, int limit);

    /**
     * 分页查询二级评论列表
     */
    List<CommentEntity> findRepliesByPage(Long parentId, int offset, int limit);

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
     * 查询一级评论及用户信息
     *
     * @param targetType
     * @param targetId
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<FindCommentItemVO> findRootCommentWithUser(Integer targetType, Long targetId, Integer pageNo, Integer pageSize);

    /**
     * 根据父评论ID分页查询子评论及用户信息
     *
     * @param parentId
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<FindChildCommentItemVO> findReplyPageWithUser(Long parentId, Integer pageNo, Integer pageSize);

    /**
     * 保存评论图片
     *
     * @param commentId
     * @param imageUrls
     */
    void saveCommentImages(Long commentId, List<String> imageUrls);

    /**
     * 根据父评论ID列表查询评论列表
     *
     * @param parentIds
     * @return
     */
    List<CommentEntity> findByParentIds(List<Long> parentIds);

    /**
     * 查询热门评论的子评论, size为子评论数量
     */
    List<CommentEntity> findRepliesByParentIdsByHot(List<Long> parentIds, int size);

    /**
     * 查询最新评论的子评论, size为子评论数量
     */
    List<CommentEntity> findRepliesByParentIdsByTime(List<Long> parentIds, int size);

    /**
     * 根据评论ID查询热门子评论
     */
    List<CommentEntity> findRepliesByParentIdByHot(Long parentId, int page, int size);

    List<CommentEntity> findRepliesByParentIdByTime(Long parentId, int page, int size);

    List<CommentEntity> findCommentsByIds(List<Long> commentIdList);
}
