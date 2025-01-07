package cn.xu.domain.comment.repository;

import cn.xu.domain.comment.model.entity.CommentEntity;
import java.util.List;

public interface ICommentRepository {
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
     */
    CommentEntity findById(Long id);

    /**
     * 删除评论
     */
    void deleteById(Long id);

    /**
     * 根据类型和目标ID查询评论列表
     */
    List<CommentEntity> findByTypeAndTargetId(Integer type, Long targetId);
}
