package cn.xu.domain.notification.event;

import cn.xu.domain.notification.model.entity.NotificationEntity;
import cn.xu.domain.notification.repository.INotificationRepository;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 调用NotificationEventPublisher.publish方法发布通知领域事件，通知事件会被NotificationEventHandler处理通知领域事件
 */
@Slf4j
@Component
public class NotificationEventHandler implements EventHandler<NotificationEvent> {

    @Resource
    private INotificationRepository notificationRepository;

    @Override
    public void onEvent(NotificationEvent event, long sequence, boolean endOfBatch) {
        log.info("[通知服务] NotificationEventHandler received event: {}", event);

        NotificationEntity notificationEntity = NotificationEntity.builder()
                .type(event.getType())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .content(event.getContent())
                .businessType(event.getBusinessType())
                .businessId(event.getBusinessId())
                .createTime(event.getCreateTime())
                .build();

        notificationRepository.save(notificationEntity);
    }
}
