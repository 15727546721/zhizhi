package cn.xu.domain.notification.handler;

import cn.xu.domain.notification.event.NotificationEvent;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象通知处理器
 * 定义通知处理的基本框架
 */
@Slf4j
public abstract class AbstractNotificationHandler {
    
    private AbstractNotificationHandler nextHandler;
    
    /**
     * 设置下一个处理器
     */
    public AbstractNotificationHandler setNext(AbstractNotificationHandler handler) {
        this.nextHandler = handler;
        return handler;
    }
    
    /**
     * 处理通知事件
     */
    public void handle(NotificationEvent event) {
        if (supports(event.getType())) {
            doHandle(event);
        } else if (nextHandler != null) {
            nextHandler.handle(event);
        } else {
            log.warn("[通知服务] 未找到合适的处理器: type={}", event.getType());
        }
    }
    
    /**
     * 判断是否支持该类型的通知
     */
    protected abstract boolean supports(NotificationType type);
    
    /**
     * 具体的处理逻辑
     */
    protected abstract void doHandle(NotificationEvent event);
} 