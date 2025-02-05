package cn.xu.domain.notification.handler;

import cn.xu.domain.notification.event.NotificationEvent;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.repository.INotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 交互类通知处理器
 * 处理评论、回复等交互类通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InteractionNotificationHandler extends AbstractNotificationHandler {

    private static final Set<NotificationType> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(
            NotificationType.COMMENT,
            NotificationType.REPLY
    ));

    private final INotificationRepository notificationRepository;

    @Override
    protected boolean supports(NotificationType type) {
        return SUPPORTED_TYPES.contains(type);
    }

    @Override
    protected void doHandle(NotificationEvent event) {
        log.info("[通知服务] 处理交互通知: type={}, receiverId={}", event.getType(), event.getReceiverId());

        if (NotificationType.COMMENT.equals(event.getType())) {
            handleCommentNotification(event);
        } else if (NotificationType.REPLY.equals(event.getType())) {
            handleReplyNotification(event);
        } else {
            log.warn("[通知服务] 未知的交互通知类型: {}", event.getType());
        }
    }

    private void handleCommentNotification(NotificationEvent event) {
        // 直接保存通知到数据库
        notificationRepository.save(event.toAggregate());
        log.debug("[通知服务] 评论通知已保存: notificationId={}", event.getNotificationId());
    }

    private void handleReplyNotification(NotificationEvent event) {
        // 直接保存通知到数据库
        notificationRepository.save(event.toAggregate());
        log.debug("[通知服务] 回复通知已保存: notificationId={}", event.getNotificationId());
    }
}