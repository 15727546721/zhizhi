package cn.xu.event.handler;

import cn.xu.config.websocket.WebSocketSessionManager;
import cn.xu.event.events.DMEvent;
import cn.xu.model.entity.User;
import cn.xu.service.user.IUserService;
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
 * 
 * <p>监听DMEvent，通过WebSocket实时推送私信通知给用户</p>
 * <p>注意：私信不再创建notification记录，只通过WebSocket推送</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateMessagePushHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;
    private final IUserService userService;

    /**
     * 处理私信发送事件
     * 
     * <p>通过WebSocket推送私信通知给接收者</p>
     */
    @Async
    @EventListener
    public void onDMSent(DMEvent event) {
        if (event.getDmEventType() != DMEvent.DMEventType.SENT) {
            return;
        }

        Long receiverId = event.getReceiverId();
        Long senderId = event.getSenderId();

        // 检查接收者是否在线
        if (!sessionManager.isOnline(receiverId)) {
            log.debug("[私信推送] 接收者不在线，跳过推送: receiverId={}", receiverId);
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
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/private-message",
                    message
            );

            log.info("[私信推送] 推送成功: sender={} → receiver={}", senderId, receiverId);

        } catch (Exception e) {
            log.error("[私信推送] 推送失败: receiverId={}, error={}", receiverId, e.getMessage());
        }
    }

    /**
     * 处理私信已读事件
     * 
     * <p>通知发送者消息已被阅读</p>
     */
    @Async
    @EventListener
    public void onDMRead(DMEvent event) {
        if (event.getDmEventType() != DMEvent.DMEventType.READ) {
            return;
        }

        // READ事件中：senderId=阅读者, receiverId=对方用户
        Long readerId = event.getSenderId();
        Long otherUserId = event.getReceiverId();

        // 检查对方是否在线
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

            log.debug("[私信推送] 已读状态推送: reader={} → other={}", readerId, otherUserId);

        } catch (Exception e) {
            log.error("[私信推送] 已读状态推送失败: error={}", e.getMessage());
        }
    }
}
