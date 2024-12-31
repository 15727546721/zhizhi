package cn.xu.infrastructure.persistent.repository;

import cn.xu.api.controller.web.comment.CommentRequest;
import cn.xu.domain.comment.model.CommentEntity;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.infrastructure.persistent.dao.ICommentDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class CommentRepository implements ICommentRepository {
    @Resource
    private ICommentDao commentDao;

    @Override
    public void addComment(CommentRequest comment) {
        log.info("添加评论: " + comment);
        commentDao.addComment(comment);
    }

    @Override
    public void replyComment(CommentRequest comment) {
        log.info("回复评论: " + comment);
        commentDao.replyComment(comment);
    }

    @Override
    public List<CommentEntity> getArticleComments(Long articleId) {
        log.info("获取文章: " + articleId + " 的评论");
        return commentDao.getArticleComments(articleId);
    }
}
