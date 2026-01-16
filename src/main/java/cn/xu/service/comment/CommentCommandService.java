package cn.xu.service.comment;

import cn.xu.event.publisher.SocialEventPublisher;
import cn.xu.model.dto.comment.SaveCommentRequest;
import cn.xu.model.entity.Comment;
import cn.xu.model.entity.Notification;
import cn.xu.model.enums.CommentType;
import cn.xu.repository.CommentRepository;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.repository.mapper.UserMapper;
import cn.xu.service.file.FileManagementService;
import cn.xu.service.follow.FollowService;
import cn.xu.service.notification.NotificationService;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 评论命令服务
 * <p>负责评论的写操作（创建、删除）</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final SocialEventPublisher socialEventPublisher;
    private final FileManagementService fileManagementService;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final FollowService followService;

    // ==================== 创建评论 ====================

    /**
     * 保存评论
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(SaveCommentRequest request) {
        validateCommentParams(request);

        Comment comment = buildComment(request);
        Long commentId = commentRepository.save(comment);
        comment.setId(commentId);

        // 更新热度分数
        updateHotScore(comment);
        commentRepository.update(comment);

        // 更新父评论回复数
        if (request.getParentId() != null && request.getParentId() > 0) {
            commentRepository.incrementReplyCount(request.getParentId());
            log.info("[评论] 增加父评论回复数 - parentId: {}", request.getParentId());
        }

        // 标记图片为正式文件
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            fileManagementService.markFilesAsPermanent(request.getImageUrls());
            log.info("[评论] 图片已标记为正式 - commentId: {}, count: {}", commentId, request.getImageUrls().size());
        }

        // 更新帖子评论数
        if (request.getTargetType() != null && request.getTargetType().equals(CommentType.POST.getValue())) {
            postMapper.updateCommentCount(request.getTargetId(), 1);
            log.info("[评论] 增加帖子评论数 - postId: {}", request.getTargetId());
        }

        // 更新用户评论数
        userMapper.increaseCommentCount(request.getUserId());

        // 发布评论事件
        publishCommentEvent(request, commentId);

        return commentId;
    }

    // ==================== 删除评论 ====================

    /**
     * 删除评论（带权限校验）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithPermission(Long commentId, Long operatorId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 权限校验：评论作者 或 帖子作者 可删除
        boolean isCommentOwner = operatorId.equals(comment.getUserId());
        boolean isPostOwner = false;
        if (comment.getTargetType() == CommentType.POST.getValue()) {
            cn.xu.model.entity.Post post = postMapper.findById(comment.getTargetId());
            if (post != null) {
                isPostOwner = operatorId.equals(post.getUserId());
            }
        }

        if (!isCommentOwner && !isPostOwner) {
            throw new BusinessException("无权删除该评论");
        }

        doDelete(comment);
    }

    /**
     * 管理员删除评论
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        doDelete(comment);
        log.info("[评论] 管理员删除成功 - commentId: {}", commentId);
    }

    /**
     * 执行删除操作
     */
    private void doDelete(Comment comment) {
        Long commentId = comment.getId();

        // 更新父评论的回复数
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            commentRepository.decrementReplyCount(comment.getParentId());
        }

        // 清理评论图片
        cleanupCommentImages(comment);

        // 统计子评论数量
        Long childCount = commentRepository.countByParentId(commentId);
        int deleteCount = 1 + childCount.intValue();

        // 清理子评论图片
        if (childCount > 0) {
            List<Comment> children = commentRepository.findByParentId(commentId);
            for (Comment child : children) {
                cleanupCommentImages(child);
                userMapper.decreaseCommentCount(child.getUserId());
            }
        }

        // 更新帖子评论数
        if (comment.getTargetType() == CommentType.POST.getValue()) {
            postMapper.updateCommentCount(comment.getTargetId(), -deleteCount);
        }

        // 更新用户评论数
        userMapper.decreaseCommentCount(comment.getUserId());

        // 发布删除事件
        socialEventPublisher.publishCommentDeleted(comment.getUserId(), comment.getTargetId(), commentId);

        // 执行删除
        commentRepository.deleteById(commentId);
        commentRepository.deleteByParentId(commentId);

        log.info("[评论] 删除完成 - commentId: {}, 共删除: {}条", commentId, deleteCount);
    }

    // ==================== 私有方法 ====================

    private void validateCommentParams(SaveCommentRequest request) {
        if (request.getTargetType() == null) {
            throw new BusinessException("评论目标类型不能为空");
        }
        if (request.getTargetId() == null) {
            throw new BusinessException("评论目标ID不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (request.getContent().length() > 1000) {
            throw new BusinessException("评论内容不能超过1000字");
        }
    }

    private Comment buildComment(SaveCommentRequest request) {
        Comment comment = new Comment();
        comment.setTargetType(Integer.valueOf(request.getTargetType()));
        comment.setTargetId(request.getTargetId());
        comment.setParentId(request.getParentId());
        comment.setUserId(request.getUserId());
        comment.setContent(request.getContent());
        comment.setImageUrls(request.getImageUrls());
        comment.setReplyUserId(request.getReplyUserId());
        comment.setLikeCount(0L);
        comment.setReplyCount(0L);
        comment.setHotScore(java.math.BigDecimal.ZERO);  // 初始化热度分数
        comment.setCreateTime(LocalDateTime.now());
        return comment;
    }

    private void updateHotScore(Comment comment) {
        // 初始热度分数计算
        comment.setHotScore(java.math.BigDecimal.ZERO);
    }

    private void cleanupCommentImages(Comment comment) {
        if (comment.getImageUrls() != null && !comment.getImageUrls().isEmpty()) {
            fileManagementService.deleteFiles(comment.getImageUrls());
            log.info("[评论] 删除图片文件 - commentId: {}", comment.getId());
        }
    }

    private void publishCommentEvent(SaveCommentRequest request, Long commentId) {
        try {
            if (request.getTargetType() == null || !request.getTargetType().equals(CommentType.POST.getValue())) {
                return;
            }

            if (request.getParentId() != null && request.getParentId() > 0) {
                // 回复评论
                socialEventPublisher.publishReplyCreated(
                        request.getUserId(),
                        request.getTargetId(),
                        commentId,
                        request.getParentId(),
                        request.getReplyUserId(),
                        request.getContent()
                );
            } else {
                // 一级评论
                socialEventPublisher.publishCommentCreated(
                        request.getUserId(),
                        request.getTargetId(),
                        commentId,
                        request.getContent()
                );
            }
        } catch (Exception e) {
            log.error("[评论] 发布评论事件失败 - commentId: {}", commentId, e);
        }

        // 发送@提及通知
        sendMentionNotifications(request);
    }

    private void sendMentionNotifications(SaveCommentRequest request) {
        List<Long> mentionedUserIds = request.getMentionedUserIds();
        if (mentionedUserIds == null || mentionedUserIds.isEmpty()) {
            return;
        }

        try {
            Long senderId = request.getUserId();
            Long postId = request.getTargetId();
            String content = request.getContent();

            Set<Long> uniqueUserIds = new LinkedHashSet<>(mentionedUserIds);
            int maxMentions = 10;
            int count = 0;

            List<Long> followingUserIds = followService.getFollowingUserIds(senderId, 500);
            Set<Long> followingSet = new HashSet<>(followingUserIds);

            for (Long receiverId : uniqueUserIds) {
                if (senderId.equals(receiverId)) {
                    continue;
                }
                if (!followingSet.contains(receiverId)) {
                    log.warn("[评论] @校验失败：用户{}未关注用户{}", senderId, receiverId);
                    continue;
                }
                if (count >= maxMentions) {
                    log.warn("[评论] @数量超限，最多{}个", maxMentions);
                    break;
                }

                Notification notification = Notification.createMentionNotification(
                        senderId, receiverId, postId, content);
                notificationService.sendNotification(notification);
                count++;
            }
        } catch (Exception e) {
            log.error("[评论] 发送@提及通知失败", e);
        }
    }
}
