package cn.xu.domain.notification.strategy.impl;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.repository.INotificationRepository;
import cn.xu.domain.notification.strategy.NotificationSendStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseNotificationStrategy implements NotificationSendStrategy {

    private final INotificationRepository notificationRepository;

    @Override
    public void send(NotificationAggregate notification) {
        try {
            notificationRepository.save(notification);
            log.info("通知已保存到数据库: id={}, type={}, userId={}", 
                    notification.getId(), 
                    notification.getType(), 
                    notification.getReceiverId());
        } catch (Exception e) {
            log.error("保存通知到数据库失败: type={}, userId={}", 
                    notification.getType(), 
                    notification.getReceiverId(),
                    e);
            throw e;
        }
    }

    @Override
    public NotificationSendStrategyType getType() {
        return NotificationSendStrategyType.DATABASE;
    }
} 