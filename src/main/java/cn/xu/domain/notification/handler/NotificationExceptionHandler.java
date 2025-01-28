package cn.xu.domain.notification.handler;

import cn.xu.domain.notification.event.NotificationEvent;
import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通知异常处理器
 * 用于处理通知事件处理过程中的异常
 */
@Slf4j
@Component
public class NotificationExceptionHandler implements ExceptionHandler<NotificationEvent> {

    @Override
    public void handleEventException(Throwable ex, long sequence, NotificationEvent event) {
        log.error("[通知服务] 处理通知事件异常, sequence={}, notificationId={}, type={}", 
                sequence, event.getNotificationId(), event.getType(), ex);
        // TODO: 可以添加重试逻辑或者将失败的事件保存到特定的队列中
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("[通知服务] 启动通知处理器异常", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("[通知服务] 关闭通知处理器异常", ex);
    }
} 