package cn.xu.domain.notification.event;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 通知事件发布服务（应用服务）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final ApplicationEventPublisher publisher;

    /**
     * 发布通知事件
     */
    public void publish(NotificationEvent event) {
        log.info("[通知服务] 发布通知事件: {}", event);
        publisher.publishEvent(event);
    }
}

