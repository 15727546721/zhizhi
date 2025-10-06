package cn.xu.domain.user.event;

import cn.xu.infrastructure.event.disruptor.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户领域事件发布器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final EventPublisher eventPublisher;

    /**
     * 发布用户注册事件
     */
    public void publishUserRegistered(UserRegisteredEvent event) {
        eventPublisher.publishEvent(event, "UserRegisteredEvent");
        log.info("发布用户注册事件: {}", event);
    }

    /**
     * 发布用户登录事件
     */
    public void publishUserLoggedIn(UserLoggedInEvent event) {
        eventPublisher.publishEvent(event, "UserLoggedInEvent");
        log.info("发布用户登录事件: {}", event);
    }

    /**
     * 发布用户更新事件
     */
    public void publishUserUpdated(UserUpdatedEvent event) {
        eventPublisher.publishEvent(event, "UserUpdatedEvent");
        log.info("发布用户更新事件: {}", event);
    }
}