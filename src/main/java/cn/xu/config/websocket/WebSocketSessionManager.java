package cn.xu.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器
 * 
 * <p>管理用户WebSocket会话，支持一个用户多设备同时在线</p>
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * userId -> sessionIds 映射
     * 一个用户可以有多个session（多设备登录）
     */
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    /**
     * sessionId -> userId 反向映射
     * 用于快速查找session对应的用户
     */
    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();

    /**
     * 添加用户会话
     * 已优化：使用 synchronized 保证 userSessions 和 sessionUsers 的原子性操作
     */
    public void addSession(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }
        
        // 使用 synchronized 保证两个 Map 操作的原子性
        synchronized (this) {
            userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                        .add(sessionId);
            sessionUsers.put(sessionId, userId);
        }
        
        log.debug("[WebSocket] 添加会话: userId={}, sessionId={}, 当前会话数={}", 
                  userId, sessionId, getSessions(userId).size());
    }

    /**
     * 移除用户会话
     * 已优化：使用 synchronized 保证 userSessions 和 sessionUsers 的原子性操作
     */
    public void removeSession(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }
        
        // 使用 synchronized 保证两个 Map 操作的原子性
        synchronized (this) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            sessionUsers.remove(sessionId);
        }
        
        log.debug("[WebSocket] 移除会话: userId={}, sessionId={}", userId, sessionId);
    }

    /**
     * 根据sessionId移除会话
     */
    public void removeSessionById(String sessionId) {
        Long userId = sessionUsers.get(sessionId);
        if (userId != null) {
            removeSession(userId, sessionId);
        }
    }

    /**
     * 获取用户的所有会话
     */
    public Set<String> getSessions(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        Set<String> sessions = userSessions.get(userId);
        return sessions != null ? new HashSet<>(sessions) : Collections.emptySet();
    }

    /**
     * 判断用户是否在线
     */
    public boolean isOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * 获取所有在线用户ID
     */
    public Set<Long> getOnlineUserIds() {
        return new HashSet<>(userSessions.keySet());
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 获取总会话数量
     */
    public int getTotalSessionCount() {
        return sessionUsers.size();
    }
}
