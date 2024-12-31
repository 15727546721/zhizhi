package cn.xu.domain.comment.repository;

import cn.xu.api.controller.web.comment.CommentRequest;
import cn.xu.domain.comment.model.CommentEntity;

import java.util.List;

public interface ICommentRepository {

    /**
     * 添加评论
     * @param comment
     */
    void addComment(CommentRequest comment);

    /**
     * 回复评论
     * @param comment
     */
    void replyComment(CommentRequest comment);

    /**
     * 获取文章的评论列表
     * @param articleId
     * @return
     */
    List<CommentEntity> getArticleComments(Long articleId);
}
