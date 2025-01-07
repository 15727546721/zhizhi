package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.exception.AppException;
import cn.xu.infrastructure.common.ResponseCode;
import cn.xu.infrastructure.persistent.dao.ICommentDao;
import cn.xu.infrastructure.persistent.po.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Repository
public class CommentRepository implements ICommentRepository {

    @Resource
    private ICommentDao commentDao;

    @Override
    public Long addComment(CommentEntity commentEntity) {
        Comment comment = convertToComment(commentEntity);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        commentDao.insert(comment);
        return comment.getId();
    }

    @Override
    public Long replyComment(CommentEntity commentEntity) {
        Comment comment = convertToComment(commentEntity);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        commentDao.insert(comment);
        return comment.getId();
    }

    @Override
    public List<CommentEntity> getArticleComments(Long articleId) {
        return commentDao.findByTypeAndTargetId(1, articleId)
                .stream()
                .map(this::convertToCommentEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentEntity> getTopicComments(Long topicId) {
        return commentDao.findByTypeAndTargetId(2, topicId)
                .stream()
                .map(this::convertToCommentEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CommentEntity findById(Long id) {
        return convertToCommentEntity(commentDao.findById(id));
    }

    @Override
    public void deleteById(Long id) {
        commentDao.deleteById(id);
    }

    @Override
    public List<CommentEntity> findByTypeAndTargetId(Integer type, Long targetId) {
        try {
            log.info("查询评论列表 - type: {}, targetId: {}", type, targetId);
            
            // 1. 调用DAO层方法获取评论列表
            List<Comment> comments = commentDao.findByTypeAndTargetId(type, targetId);
            
            // 2. 转换为领域实体
            if (comments == null || comments.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 3. 将PO对象转换为领域实体对象
            List<CommentEntity> commentEntities = comments.stream()
                    .map(this::convertToCommentEntity)
                    .collect(Collectors.toList());
                
            log.info("查询到评论数量: {}", commentEntities.size());
            return commentEntities;
                
        } catch (Exception e) {
            log.error("查询评论列表失败 - type: {}, targetId: {}", type, targetId, e);
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "查询评论列表失败：" + e.getMessage());
        }
    }

    private Comment convertToComment(CommentEntity entity) {
        if (entity == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setId(entity.getId());
        comment.setType(entity.getType());
        comment.setTargetId(entity.getTargetId());
        comment.setParentId(entity.getParentId());
        comment.setUserId(entity.getUserId());
        comment.setReplyToUserId(entity.getReplyToUserId());
        comment.setContent(entity.getContent());
        comment.setCreateTime(entity.getCreateTime());
        comment.setUpdateTime(entity.getUpdateTime());
        return comment;
    }

    private CommentEntity convertToCommentEntity(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentEntity.builder()
                .id(comment.getId())
                .type(comment.getType())
                .targetId(comment.getTargetId())
                .parentId(comment.getParentId())
                .userId(comment.getUserId())
                .replyToUserId(comment.getReplyToUserId())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .build();
    }
}
