package cn.xu.domain.comment.event;

import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.comment.service.HotScoreService;
//import cn.xu.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 评论事件监听处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventListener {

    private final ICommentRepository commentRepository;
    private final HotScoreService hotScoreService;
//    private final NotificationService notificationService;

    /**
     * 处理评论创建事件（事务提交后执行）
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        try {
            log.info("处理评论创建事件: {}", event);

            if (event.getParentId() == null) {
                // 根评论处理
            }
            // 1. 更新目标对象的评论计数
            updateTargetCommentCount(CommentType.valueOf(event.getTargetType()), event.getTargetId(), 1);

            // 2. 更新热度分数（异步）
            hotScoreService.updateHotScore(event.getCommentId());

            // 3. 如果是回复，发送通知给被回复用户
//            if (event.getLevel() == 2) {
//                CommentEntity comment = commentRepository.findById(event.getCommentId());
//                if (comment != null && comment.getReplyUserId() != null) {
//                    notificationService.sendReplyNotification(
//                            comment.getUserId(),
//                            comment.getReplyUserId(),
//                            comment.getId(),
//                            comment.getContent()
//                    );
//                }
//            }
        } catch (Exception e) {
            log.error("处理评论创建事件失败", e);
        }
    }

    /**
     * 处理评论点赞事件
     */
    @Async
    @EventListener
    public void handleCommentLikedEvent(CommentLikedEvent event) {
        try {
            log.info("处理评论点赞事件: {}", event);

            // 1. 更新Redis中的点赞计数
//            if (event.isLike()) {
//                commentRedisRepository.incrementLikeCount(event.getCommentId());
//            } else {
//                commentRedisRepository.decrementLikeCount(event.getCommentId());
//            }

            // 2. 更新热度分数
            hotScoreService.updateHotScore(event.getCommentId());

            // 3. 发送点赞通知
//            if (event.isLike()) {
//                notificationService.sendLikeNotification(
//                        event.getUserId(),
//                        commentRepository.findUserIdById(event.getCommentId()),
//                        event.getCommentId()
//                );
//            }
        } catch (Exception e) {
            log.error("处理评论点赞事件失败", e);
        }
    }

    /**
     * 处理评论删除事件
     */
    @Async
    @EventListener
    public void handleCommentDeletedEvent(CommentDeletedEvent event) {
        try {
            log.info("处理评论删除事件: {}", event);

            // 1. 更新目标对象的评论计数
//            updateTargetCommentCount(event.getTargetType(), event.getTargetId(), -1);

//            // 2. 清理相关数据
//            commentImageRepository.deleteByCommentId(event.getCommentId());
//            commentRedisRepository.clearLikeCount(event.getCommentId());
//
//            // 3. 如果是根评论，删除所有子评论
//            if (event.isRootComment()) {
//                commentRepository.deleteAllReplies(event.getCommentId());
//            }
        } catch (Exception e) {
            log.error("处理评论删除事件失败", e);
        }
    }

    private void updateTargetCommentCount(CommentType type, Long targetId, int increment) {
        switch (type) {
//            case VIDEO:
//                videoService.updateCommentCount(targetId, increment);
//                break;
//            case ARTICLE:
//                articleService.updateCommentCount(targetId, increment);
//                break;
//            case ESSAY:
//                essayService.updateCommentCount(targetId, increment);
//                break;
            default:
                log.warn("未知评论目标类型: {}", type);
        }
    }
}