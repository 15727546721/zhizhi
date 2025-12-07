package cn.xu.event.publisher;

import cn.xu.event.core.EventBus;
import cn.xu.event.events.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户事件发布器
 * 
 * <p>负责发布用户类事件：
 * <ul>
 *   <li>注册</li>
 *   <li>登录</li>
 *   <li>资料更新</li>
 * </ul>
 *
 * 
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {
    
    private final EventBus eventBus;
    
    /**
     * 发布用户注册事件
     */
    public void publishRegistered(Long userId, String nickname) {
        eventBus.publish(UserEvent.registered(userId, nickname));
    }
    
    /**
     * 发布用户登录事件
     */
    public void publishLoggedIn(Long userId) {
        eventBus.publish(UserEvent.loggedIn(userId));
    }
    
    /**
     * 发布用户资料更新事件
     */
    public void publishUpdated(Long userId, String nickname, String avatar) {
        eventBus.publish(UserEvent.updated(userId, nickname, avatar));
    }
}
