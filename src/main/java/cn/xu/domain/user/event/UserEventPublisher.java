package cn.xu.domain.user.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 用户事件发布器
 * 使用Spring Event机制发布用户相关事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布用户注册事件
     */
    public void publishUserRegistered(UserRegisteredEvent event) {
        log.debug("发布用户注册事件: userId={}", event.getUserId());
        eventPublisher.publishEvent(event);
    }

    /**
     * 发布用户登录事件
     */
    public void publishUserLoggedIn(UserLoggedInEvent event) {
        log.debug("发布用户登录事件: userId={}", event.getUserId());
        eventPublisher.publishEvent(event);
    }

    /**
     * 发布用户更新事件
     */
    public void publishUserUpdated(UserUpdatedEvent event) {
        log.debug("发布用户更新事件: userId={}", event.getUserId());
        eventPublisher.publishEvent(event);
    }
}