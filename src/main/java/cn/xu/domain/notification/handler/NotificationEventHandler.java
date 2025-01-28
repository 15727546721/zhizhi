    //package cn.xu.domain.notification.handler;
    //
    //import cn.xu.domain.notification.event.NotificationEvent;
    //import com.lmax.disruptor.EventHandler;
    //import lombok.extern.slf4j.Slf4j;
    //import org.springframework.stereotype.Component;
    //
    //import javax.annotation.PostConstruct;
    //
    ///**
    // * 通知事件处理器
    // * 使用 Disruptor 处理异步通知事件
    // */
    //@Slf4j
    //@Component
    //public class NotificationEventHandler implements EventHandler<NotificationEvent> {
    //
    //    private final SystemNotificationHandler systemNotificationHandler;
    //    private final InteractionNotificationHandler interactionNotificationHandler;
    //    private AbstractNotificationHandler handlerChain;
    //
    //    public NotificationEventHandler(
    //            SystemNotificationHandler systemNotificationHandler,
    //            InteractionNotificationHandler interactionNotificationHandler) {
    //        this.systemNotificationHandler = systemNotificationHandler;
    //        this.interactionNotificationHandler = interactionNotificationHandler;
    //    }
    //
    //    @PostConstruct
    //    public void init() {
    //        // 构建处理器链
    //        handlerChain = systemNotificationHandler;
    //        handlerChain.setNext(interactionNotificationHandler);
    //    }
    //
    //    @Override
    //    public void onEvent(NotificationEvent event, long sequence, boolean endOfBatch) throws Exception {
    //        try {
    //            log.info("[通知服务] 开始处理通知事件: type={}, sequence={}", event.getType(), sequence);
    //
    //            // 使用处理器链处理事件
    //            handlerChain.handle(event);
    //
    //            if (endOfBatch) {
    //                log.info("[通知服务] 批次处理完成: sequence={}", sequence);
    //            }
    //        } catch (Exception e) {
    //            log.error("[通知服务] 通知事件处理失败: notificationId={}, type={}, sequence={}",
    //                    event.getNotificationId(), event.getType(), sequence, e);
    //            throw e; // 抛出异常，让异常处理器处理
    //        }
    //    }
    //}