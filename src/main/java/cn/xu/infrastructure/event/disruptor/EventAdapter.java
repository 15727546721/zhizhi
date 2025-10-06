package cn.xu.infrastructure.event.disruptor;

import cn.xu.infrastructure.event.EventMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 事件适配器
 * 将Disruptor事件转换为Spring事件
 */
@Slf4j
@Component
public class EventAdapter {
    
    private final ApplicationEventPublisher eventPublisher;
    private final EventMonitorService eventMonitorService;
    
    public EventAdapter(ApplicationEventPublisher eventPublisher, EventMonitorService eventMonitorService) {
        this.eventPublisher = eventPublisher;
        this.eventMonitorService = eventMonitorService;
    }
    
    /**
     * 发布Spring事件
     * @param event 事件对象
     */
    public void publishSpringEvent(Object event) {
        try {
            log.debug("发布Spring事件: event={}", event);
            
            // 发布事件
            eventPublisher.publishEvent(event);
            
            // 记录事件处理
            if (event instanceof EventDataWrapper) {
                eventMonitorService.recordEventProcessed(((EventDataWrapper) event).getEventType());
            }
        } catch (Exception e) {
            // 记录事件处理失败
            if (event instanceof EventDataWrapper) {
                eventMonitorService.recordEventError(((EventDataWrapper) event).getEventType());
            }
            
            log.error("发布Spring事件失败: event={}", event, e);
        }
    }
}