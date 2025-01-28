package cn.xu.domain.notification.event.factory;

import cn.xu.domain.notification.event.NotificationEvent;
import com.lmax.disruptor.EventFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 通知事件工厂
 * 用于创建通知事件实例
 *
 * @author xuhh
 * @date 2024/03/21
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationEventFactory implements EventFactory<NotificationEvent> {
    
    private static final NotificationEventFactory INSTANCE = new NotificationEventFactory();
    
    /**
     * 获取工厂实例
     */
    public static NotificationEventFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public NotificationEvent newInstance() {
        return new NotificationEvent();
    }
} 