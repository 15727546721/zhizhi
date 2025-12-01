package cn.xu.event.listener;

import cn.xu.event.EventMonitorService;
import cn.xu.event.bus.SpringEventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 通用事件监听器
 * 处理通过EventBus发布的通用事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenericEventListener {
    
    private final EventMonitorService eventMonitorService;
    
    /**
     * 异步处理通用事件
     */
    @Async
    @EventListener
    public void handleGenericEvent(SpringEventBus.GenericEvent event) {
        try {
            String eventType = event.getEventType();
            Object eventData = event.getEventData();
            
            log.info("处理通用事件: eventType={}, eventData={}", eventType, eventData);
            
            // 记录事件处理
            eventMonitorService.recordEventProcessed(eventType);
            
            // 这里可以根据eventType分发到不同的处理逻辑
            
        } catch (Exception e) {
            log.error("处理通用事件失败: event={}", event, e);
            eventMonitorService.recordEventError(event.getEventType());
        }
    }
}
