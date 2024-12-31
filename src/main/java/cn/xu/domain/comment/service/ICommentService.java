package cn.xu.domain.comment.service;

import cn.xu.api.controller.web.comment.CommentRequest;
import cn.xu.domain.comment.model.CommentEntity;

import java.util.List;

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
     * 获取文章评论列表
     *
     * @return 文章评论列表
     */
    List<CommentEntity> getArticleComments(Long articleId);
}
