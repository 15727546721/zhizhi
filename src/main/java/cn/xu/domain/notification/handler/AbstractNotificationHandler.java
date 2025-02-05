package cn.xu.domain.notification.handler;

import cn.xu.domain.notification.event.NotificationEvent;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象通知处理器
 * 定义通知处理的模板方法
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
    public final void handle(NotificationEvent event) {
        try {
            // 1. 检查是否支持该类型的通知
            if (!supports(event.getType())) {
                log.debug("[通知服务] 不支持的通知类型: {}", event.getType());
                return;
            }

            // 2. 执行具体的处理逻辑
            doHandle(event);
            
        } catch (Exception e) {
            log.error("[通知服务] 处理通知失败: type={}, error={}", event.getType(), e.getMessage(), e);
            throw e;
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