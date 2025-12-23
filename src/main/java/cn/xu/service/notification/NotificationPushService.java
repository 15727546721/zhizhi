package cn.xu.service.notification;

import cn.xu.config.websocket.WebSocketSessionManager;
import cn.xu.model.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知推送服务
 * 
 * <p>负责通过WebSocket推送通知给用户</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPushService {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;
    private final NotificationService notificationService;

    /**
     * 推送通知给指定用户
     * 
     * @param userId 用户ID
     * @param notification 通知实体
     */
    public void pushToUser(Long userId, Notification notification) {
        if (userId == null || notification == null) {
            return;
        }
        
        if (!sessionManager.isOnline(userId)) {
            log.debug("[WebSocket推送] 用户不在线，跳过推送: userId={}", userId);
            return;
        }
        
        try {
            Map<String, Object> message = buildNotificationMessage(notification);
            
            // 推送通知内容
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    message
            );
            
            // 同时推送未读数量
            pushUnreadCount(userId);
            
            log.info("[WebSocket推送] 推送成功: userId={}, type={}", userId, notification.getType());
            
        } catch (Exception e) {
            log.error("[WebSocket推送] 推送失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 推送未读数量给指定用户
     */
    public void pushUnreadCount(Long userId) {
        if (userId == null || !sessionManager.isOnline(userId)) {
            return;
        }
        
        try {
            long count = notificationService.getUnreadCount(userId);
            Map<String, Object> message = new HashMap<>();
            message.put("count", count);
            message.put("timestamp", LocalDateTime.now().toString());
            
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/unread-count",
                    message
            );
            
            log.debug("[WebSocket推送] 未读数推送: userId={}, count={}", userId, count);
            
        } catch (Exception e) {
            log.error("[WebSocket推送] 未读数推送失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 构建通知消息
     */
    private Map<String, Object> buildNotificationMessage(Notification notification) {
        Map<String, Object> message = new HashMap<>();
        message.put("id", notification.getId());
        message.put("type", notification.getType());
        message.put("typeName", getTypeName(notification.getType()));
        message.put("title", notification.getTitle());
        message.put("content", notification.getContent());
        message.put("senderId", notification.getSenderId());
        message.put("businessType", notification.getBusinessType());
        message.put("businessId", notification.getBusinessId());
        message.put("createTime", notification.getCreateTime() != null ? 
                notification.getCreateTime().toString() : LocalDateTime.now().toString());
        return message;
    }

    /**
     * 获取通知类型名称
     */
    private String getTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 0: return "系统通知";
            case 1: return "点赞";
            case 2: return "收藏";
            case 3: return "评论";
            case 4: return "回复";
            case 5: return "关注";
            case 6: return "@提及";
            default: return "其他";
        }
    }
}
