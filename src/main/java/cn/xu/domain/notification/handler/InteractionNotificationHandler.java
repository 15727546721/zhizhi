//package cn.xu.domain.notification.handler;
//
//import cn.xu.domain.notification.event.NotificationEvent;
//import cn.xu.domain.notification.model.valueobject.NotificationType;
//import cn.xu.domain.notification.service.INotificationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.Set;
//
///**
// * 交互类通知处理器
// * 处理点赞、评论、收藏等交互类通知
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class InteractionNotificationHandler extends AbstractNotificationHandler {
//
//    private static final Set<NotificationType> SUPPORTED_TYPES = Set.of(
//            NotificationType.LIKE,
//            NotificationType.COMMENT,
//            NotificationType.FAVORITE,
//            NotificationType.REPLY
//    );
//
//    private final INotificationService notificationService;
//
//    @Override
//    protected boolean supports(NotificationType type) {
//        return SUPPORTED_TYPES.contains(type);
//    }
//
//    @Override
//    protected void doHandle(NotificationEvent event) {
//        log.info("[通知服务] 处理交互通知: type={}, receiverId={}", event.getType(), event.getReceiverId());
//
//        switch (event.getType()) {
//            case LIKE:
//                notificationService.sendLikeNotification(event);
//                break;
//            case COMMENT:
//                notificationService.sendCommentNotification(event);
//                break;
//            case FAVORITE:
//                notificationService.sendFavoriteNotification(event);
//                break;
//            case REPLY:
//                notificationService.sendReplyNotification(event);
//                break;
//            default:
//                log.warn("[通知服务] 未知的交互通知类型: {}", event.getType());
//        }
//    }
//}