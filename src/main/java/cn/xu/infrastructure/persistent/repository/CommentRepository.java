package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import cn.xu.infrastructure.persistent.dao.ICommentDao;
import cn.xu.infrastructure.persistent.po.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        try {
            log.info("查询评论 - id: {}", id);
            return commentDao.selectById(id);
        } catch (Exception e) {
            log.error("查询评论失败 - id: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询评论失败：" + e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            log.info("删除评论 - id: {}", id);
            commentDao.deleteById(id);
            log.info("删除评论成功 - id: {}", id);
        } catch (Exception e) {
            log.error("删除评论失败 - id: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除评论失败：" + e.getMessage());
        }
    }

    @Override
    public List<CommentEntity> findByTypeAndTargetId(Integer type, Long targetId) {
        try {
            log.info("查询评论列表 - type: {}, targetId: {}", type, targetId);

            List<Comment> comments = commentDao.findByTypeAndTargetId(type, targetId);
            if (comments == null || comments.isEmpty()) {
                return new ArrayList<>();
            }

            List<CommentEntity> commentEntities = comments.stream()
                    .map(this::convertToCommentEntity)
                    .collect(Collectors.toList());

            log.info("查询到评论数量: {}", commentEntities.size());
            return commentEntities;

        } catch (Exception e) {
            log.error("查询评论列表失败 - type: {}, targetId: {}", type, targetId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询评论列表失败：" + e.getMessage());
        }
    }

    @Override
    public List<CommentEntity> findByParentId(Long parentId) {
        try {
            log.info("查询子评论列表 - parentId: {}", parentId);

            List<Comment> comments = commentDao.findByParentId(parentId);
            if (comments == null || comments.isEmpty()) {
                return new ArrayList<>();
            }

            List<CommentEntity> commentEntities = comments.stream()
                    .map(this::convertToCommentEntity)
                    .collect(Collectors.toList());

            log.info("查询到子评论数量: {}", commentEntities.size());
            return commentEntities;

        } catch (Exception e) {
            log.error("查询子评论列表失败 - parentId: {}", parentId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询子评论列表失败：" + e.getMessage());
        }
    }

    @Override
    public void batchDelete(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }
        try {
            log.info("开始批量删除评论 - commentIds: {}", commentIds);
            int deletedCount = commentDao.batchDelete(commentIds);
            log.info("批量删除评论完成 - 删除数量: {}", deletedCount);

            // 如果删除数量与预期不符，抛出异常
            if (deletedCount != commentIds.size()) {
                log.error("批量删除评论数量不匹配 - 预期: {}, 实际: {}", commentIds.size(), deletedCount);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除评论数量不匹配");
            }
        } catch (Exception e) {
            log.error("批量删除评论失败 - commentIds: {}", commentIds, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量删除评论失败：" + e.getMessage());
        }
    }

    public void deleteByTopicId(Long topicId) {
        try {
            log.info("删除与话题相关的评论 - topicId: {}", topicId);
            commentDao.deleteByTypeAndTargetId(2, topicId);
        } catch (Exception e) {
            log.error("删除与话题相关的评论失败 - topicId: {}", topicId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除与话题相关的评论失败");
        }
    }

    public List<CommentEntity> findRootComments(Long targetId, Integer type) {
        try {
            log.info("查询一级评论列表 - targetId: {}, type: {}", targetId, type);
            List<Comment> comments = commentDao.findRootComments(targetId, type);
            if (comments == null || comments.isEmpty()) {
                return new ArrayList<>();
            }
            return comments.stream()
                    .map(this::convertToCommentEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询一级评论列表失败 - targetId: {}, type: {}", targetId, type, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询评论列表失败：" + e.getMessage());
        }
    }

    public List<CommentEntity> findRepliesByPage(Long parentId, int offset, int limit) {
        try {
            log.info("分页查询二级评论列表 - parentId: {}, offset: {}, limit: {}", parentId, offset, limit);
            List<Comment> replies = commentDao.findRepliesByPage(parentId, offset, limit);
            if (replies == null || replies.isEmpty()) {
                return new ArrayList<>();
            }
            return replies.stream()
                    .map(this::convertToCommentEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("分页查询二级评论列表失败 - parentId: {}", parentId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询回复列表失败：" + e.getMessage());
        }
    }

    /**
     * 统计一级评论总数
     *
     * @param type 评论类型（可选）
     * @return 评论总数
     */
    @Override
    public long countRootComments(Integer type, Long userId) {
        return commentDao.countRootComments(type, userId);
    }

    /**
     * 分页查询一级评论列表
     *
     * @param type   评论类型（可选）
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit  每页数量
     * @return 一级评论列表
     */
    @Override
    public List<CommentEntity> findRootCommentsByPage(Integer type, Long userId, int offset, int limit) {
        List<Comment> comments = commentDao.findRootCommentsByPage(type, userId, offset, limit);
        return comments.stream()
                .map(this::convertToCommentEntity)
                .collect(Collectors.toList());
    }

    /**
     * 将PO对象转换为领域实体对象
     */
    private CommentEntity convertToEntity(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentEntity.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .type(comment.getType())
                .targetId(comment.getTargetId())
                .content(comment.getContent())
                .parentId(comment.getParentId())
                .replyUserId(comment.getReplyUserId())
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .build();
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
        comment.setReplyUserId(entity.getReplyUserId());
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
                .replyUserId(comment.getReplyUserId())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .build();
    }

    @Override
    public void deleteByParentId(Long parentId) {
        try {
            log.info("删除子评论 - parentId: {}", parentId);
            commentDao.deleteByParentId(parentId);
            log.info("删除子评论成功 - parentId: {}", parentId);
        } catch (Exception e) {
            log.error("删除子评论失败 - parentId: {}", parentId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除子评论失败：" + e.getMessage());
        }
    }
}
