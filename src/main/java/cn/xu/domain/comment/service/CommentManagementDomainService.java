package cn.xu.domain.comment.service;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.comment.event.CommentDeletedEvent;
import cn.xu.domain.comment.event.CommentEventPublisher;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 评论管理领域服务
 * 负责评论删除、权限验证等管理相关的业务逻辑，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentManagementDomainService {

    private final ICommentRepository commentRepository;
    private final CommentEventPublisher commentEventPublisher;
    private final IUserService userService;
    private final CommentCacheDomainService commentCacheDomainService;

    /**
     * 删除评论（用户删除自己的评论）
     * @param commentId 评论ID
     */
    public void deleteUserComment(Long commentId) {
        CommentEntity comment = validateCommentExists(commentId);
        validateDeletePermission(comment);
        executeCommentDeletion(comment);
    }

    /**
     * 删除评论（管理员删除）
     * @param commentId 评论ID
     */
    public void deleteCommentByAdmin(Long commentId) {
        CommentEntity comment = validateCommentExists(commentId);
        executeCommentDeletion(comment);
    }

    /**
     * 验证评论是否存在
     */
    public CommentEntity validateCommentExists(Long commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }
        
        CommentEntity comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论不存在");
        }
        
        return comment;
    }

    /**
     * 验证删除权限
     */
    private void validateDeletePermission(CommentEntity comment) {
        Long currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户未登录");
        }
        
        if (!comment.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "只能删除自己发布的评论");
        }
    }

    /**
     * 执行评论删除
     */
    private void executeCommentDeletion(CommentEntity comment) {
        try {
            int deletedCount = 1;
            
            // 如果是根评论，同时删除所有子评论
            if (comment.getParentId() == null) {
                deletedCount += commentRepository.deleteByParentId(comment.getId());
            }
            
            // 删除评论本身
            commentRepository.deleteById(comment.getId());

            // 从缓存中移除评论
            commentCacheDomainService.removeCommentFromHotRank(
                cn.xu.domain.comment.model.valueobject.CommentType.valueOf(comment.getTargetType()),
                comment.getTargetId(),
                comment.getId()
            );

            // 发布删除事件
            commentEventPublisher.publishCommentDeletedEvent(CommentDeletedEvent.builder()
                    .commentId(comment.getId())
                    .targetType(comment.getTargetType())
                    .targetId(comment.getTargetId())
                    .isRootComment(comment.getParentId() == null)
                    .build());

            log.info("评论删除成功 - commentId: {}, 删除数量: {}", comment.getId(), deletedCount);
        } catch (Exception e) {
            log.error("删除评论失败 - commentId: {}", comment.getId(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除评论失败：" + e.getMessage());
        }
    }
}