package cn.xu.event.handler;

import cn.xu.cache.service.CacheService;
import cn.xu.event.events.CommentCreatedInternalEvent;
import cn.xu.event.publisher.SocialEventPublisher;
import cn.xu.model.dto.comment.SaveCommentRequest;
import cn.xu.model.entity.Notification;
import cn.xu.model.enums.CommentType;
import cn.xu.service.follow.FollowService;
import cn.xu.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 评论事件监听器
 * <p>处理事务提交后的异步操作：发送通知、清除缓存等</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventListener {

    private final SocialEventPublisher socialEventPublisher;
    private final NotificationService notificationService;
    private final FollowService followService;
    private final CacheService cacheService;

    private static final String COMMENT_HOT_PAGE_KEY_PREFIX = "comment:hot:page:";

    /**
     * 评论创建后处理（事务提交后执行）
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedInternalEvent event) {
        SaveCommentRequest request = event.getRequest();
        Long commentId = event.getCommentId();

        log.info("[评论事件] 开始处理评论创建后事件 - commentId: {}", commentId);

        try {
            // 1. 清除评论缓存
            clearCommentCache(request.getTargetType(), request.getTargetId());

            // 2. 发布评论事件（用于通知等）
            publishCommentEvent(request, commentId);

            // 3. 发送@提及通知
            sendMentionNotifications(request);

            log.info("[评论事件] 评论创建后事件处理完成 - commentId: {}", commentId);
        } catch (Exception e) {
            log.error("[评论事件] 评论创建后事件处理失败 - commentId: {}", commentId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 清除评论缓存
     */
    public void clearCommentCache(Integer targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            return;
        }
        try {
            String pattern = String.format("%s%d:%d:*", COMMENT_HOT_PAGE_KEY_PREFIX, targetType, targetId);
            cacheService.evictByPattern(pattern);
            log.debug("[评论缓存] 清除缓存 - pattern: {}", pattern);
        } catch (Exception e) {
            log.warn("[评论缓存] 清除缓存失败 - targetType: {}, targetId: {}", targetType, targetId, e);
        }
    }

    /**
     * 发布评论事件
     */
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
            log.error("[评论事件] 发布评论事件失败 - commentId: {}", commentId, e);
        }
    }

    /**
     * 发送@提及通知
     */
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
                    log.warn("[评论事件] @校验失败：用户{}未关注用户{}", senderId, receiverId);
                    continue;
                }
                if (count >= maxMentions) {
                    log.warn("[评论事件] @数量超限，最多{}个", maxMentions);
                    break;
                }

                Notification notification = Notification.createMentionNotification(
                        senderId, receiverId, postId, content);
                notificationService.sendNotification(notification);
                count++;
            }
        } catch (Exception e) {
            log.error("[评论事件] 发送@提及通知失败", e);
        }
    }
}
