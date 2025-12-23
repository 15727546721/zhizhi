package cn.xu.config.websocket;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

/**
 * WebSocket认证拦截器
 * 
 * <p>在STOMP CONNECT时进行Sa-Token认证</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final WebSocketSessionManager sessionManager;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        
        if (StompCommand.CONNECT.equals(command)) {
            // 从Header获取token
            List<String> tokenList = accessor.getNativeHeader("token");
            String token = (tokenList != null && !tokenList.isEmpty()) ? tokenList.get(0) : null;
            
            if (token == null || token.isEmpty()) {
                log.warn("[WebSocket] 连接失败: 缺少token");
                throw new IllegalArgumentException("缺少认证token");
            }
            
            try {
                // 使用Sa-Token验证token
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId == null) {
                    log.warn("[WebSocket] 连接失败: token无效");
                    throw new IllegalArgumentException("token无效或已过期");
                }
                
                Long userId = Long.valueOf(loginId.toString());
                String sessionId = accessor.getSessionId();
                
                // 设置用户身份
                accessor.setUser(new WebSocketPrincipal(userId));
                
                // 记录会话
                sessionManager.addSession(userId, sessionId);
                
                log.info("[WebSocket] 用户连接成功: userId={}, sessionId={}", userId, sessionId);
                
            } catch (Exception e) {
                log.error("[WebSocket] 认证失败: {}", e.getMessage());
                throw new IllegalArgumentException("认证失败: " + e.getMessage());
            }
            
        } else if (StompCommand.DISCONNECT.equals(command)) {
            // 断开连接时清理会话
            Principal user = accessor.getUser();
            if (user instanceof WebSocketPrincipal) {
                Long userId = ((WebSocketPrincipal) user).getUserId();
                String sessionId = accessor.getSessionId();
                sessionManager.removeSession(userId, sessionId);
                log.info("[WebSocket] 用户断开连接: userId={}, sessionId={}", userId, sessionId);
            }
        }
        
        return message;
    }

    /**
     * 自定义Principal，用于标识WebSocket用户
     */
    public static class WebSocketPrincipal implements Principal {
        private final Long userId;

        public WebSocketPrincipal(Long userId) {
            this.userId = userId;
        }

        @Override
        public String getName() {
            return userId.toString();
        }

        public Long getUserId() {
            return userId;
        }
    }
}
