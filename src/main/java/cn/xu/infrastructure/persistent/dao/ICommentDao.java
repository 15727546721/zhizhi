package cn.xu.infrastructure.persistent.dao;

import cn.xu.api.controller.web.comment.CommentRequest;
import cn.xu.domain.comment.model.CommentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ICommentDao {
    /**
     * 添加评论
     *
     * @param comment 评论请求参数
     */
    void addComment(CommentRequest comment);

    /**
     * 回复评论
     *
     * @param comment
     */
    void replyComment(CommentRequest comment);

    /**
     * 获取文章评论列表
     *
     * @param articleId 文章ID
     * @return 评论列表
     */
    List<CommentEntity> getArticleComments(@Param("articleId") Long articleId);
}
