package cn.xu.domain.comment.service;

import cn.xu.api.controller.web.comment.request.CommentRequest;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;

import java.util.List;

/**
 * 评论服务接口
 *
 * @author xuhongzu
 * @date 2024/03/16
 */
public interface ICommentService {
    /**
     * 添加评论
     *
     * @param comment 评论请求参数
     */
    void addComment(CommentRequest comment);

    /**
     * 回复评论
     *
     * @param comment 评论请求参数
     */
    void replyComment(CommentRequest comment);

    /**
     * 根据类型和目标ID获取评论列表
     *
     * @param type     评论类型
     * @param targetId 目标ID（文章ID或话题ID）
     * @return 评论列表（已构建好父子关系）
     */
    List<CommentEntity> getCommentsByTypeAndTargetId(CommentType type, Long targetId);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    void deleteComment(Long commentId, Long userId);
}
