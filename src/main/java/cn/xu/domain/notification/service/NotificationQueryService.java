package cn.xu.domain.notification.service;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.repository.INotificationRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.request.PageRequest;
import cn.xu.infrastructure.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {
    private final INotificationRepository notificationRepository;
    
    public PageResponse<List<NotificationAggregate>> getUserNotifications(Long userId, PageRequest pageRequest) {
        int offset = (pageRequest.getPageNo() - 1) * pageRequest.getPageSize();
        int limit = pageRequest.getPageSize();
        
        List<NotificationAggregate> notificationAggregateList = notificationRepository.findByReceiverIdOrderByCreatedTimeDesc(
            userId, offset, limit);
            
        return PageResponse.of(pageRequest.getPageNo(), pageRequest.getPageSize(),
                notificationRepository.countByReceiverIdAndReadFalse(userId),
                notificationAggregateList);
    }
    
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverIdAndReadFalse(userId);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId) {
        NotificationAggregate notification = notificationRepository.findById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        notification.markAsRead();
        notificationRepository.save(notification);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
} 