package cn.xu.event.handler;

import cn.xu.config.websocket.WebSocketSessionManager;
import cn.xu.event.events.DMEvent;
import cn.xu.model.entity.User;
import cn.xu.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 私信WebSocket推送处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateMessagePushHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;
    private final UserService userService;

    /**
     * 处理私信发送事件
     */
    @Async
    @EventListener
    public void onDMSent(DMEvent event) {
        if (event.getDmEventType() != DMEvent.DMEventType.SENT) {
            return;
        }

        Long receiverId = event.getReceiverId();
        Long senderId = event.getSenderId();
        
        log.info("[WebSocket推送] 收到私信事件 sender:{} → receiver:{} messageId:{}", 
                senderId, receiverId, event.getMessageId());

        // 检查接收者是否在线
        boolean isOnline = sessionManager.isOnline(receiverId);
        log.info("[WebSocket推送] 接收者在线状态: receiverId={} isOnline={}", receiverId, isOnline);
        
        if (!isOnline) {
            log.info("[WebSocket推送] 接收者不在线，跳过推送");
            return;
        }

        try {
            // 获取发送者信息
            User sender = userService.getUserById(senderId);
            
            // 构建推送消息
            Map<String, Object> message = new HashMap<>();
            message.put("type", "private_message");
            message.put("senderId", senderId);
            message.put("senderName", sender != null ? sender.getNickname() : "未知用户");
            message.put("senderAvatar", sender != null ? sender.getAvatar() : null);
            message.put("messageId", event.getMessageId());
            message.put("content", event.getContentPreview());
            message.put("isGreeting", event.isGreeting());
            message.put("timestamp", LocalDateTime.now().toString());

            // 推送给接收者
            String destination = "/queue/private-message";
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    destination,
                    message
            );

            log.info("[WebSocket推送] 推送成功 sender:{} → receiver:{} destination:/user/{}{}", 
                    senderId, receiverId, receiverId, destination);

        } catch (Exception e) {
            log.error("[WebSocket推送] 推送失败 receiverId:{} error:{}", receiverId, e.getMessage(), e);
        }
    }

    /**
     * 处理私信已读事件
     */
    @Async
    @EventListener
    public void onDMRead(DMEvent event) {
        if (event.getDmEventType() != DMEvent.DMEventType.READ) {
            return;
        }

        Long readerId = event.getSenderId();
        Long otherUserId = event.getReceiverId();

        if (!sessionManager.isOnline(otherUserId)) {
            return;
        }

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "message_read");
            message.put("readerId", readerId);
            message.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSendToUser(
                    otherUserId.toString(),
                    "/queue/private-message",
                    message
            );

            log.info("[WebSocket推送] 已读状态推送 reader:{} → other:{}", readerId, otherUserId);

        } catch (Exception e) {
            log.error("[WebSocket推送] 已读状态推送失败: {}", e.getMessage());
        }
    }
}
