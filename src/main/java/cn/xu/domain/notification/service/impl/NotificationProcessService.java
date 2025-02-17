package cn.xu.domain.notification.service.impl;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.repository.INotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProcessService {
    
    private final INotificationRepository notificationRepository;
    
    @Async
    @Transactional
    public void processNotificationAsync(NotificationAggregate notification) {
        try {
            // 1. 保存通知到数据库
            notificationRepository.save(notification);
            log.info("通知已保存到数据库: type={}, userId={}", 
                notification.getType(), notification.getReceiverId());
        } catch (Exception e) {
            log.error("处理通知失败: type={}, userId={}", 
                notification.getType(), notification.getReceiverId(), e);
            throw e;
        }
    }

} 