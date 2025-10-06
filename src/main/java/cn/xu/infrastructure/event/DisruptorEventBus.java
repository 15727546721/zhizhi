package cn.xu.infrastructure.event;

import cn.xu.infrastructure.event.disruptor.EventDataWrapper;
import cn.xu.infrastructure.event.disruptor.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 基于Disruptor的事件总线实现
 * 模拟MQ的功能，提供高性能的事件发布和订阅机制
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DisruptorEventBus implements EventBus {
    
    private final EventPublisher eventPublisher;
    private final DisruptorEventService eventService;
    private final EventConfig eventConfig;
    
    @Override
    public void publish(Object event, String eventType) {
        if (!eventConfig.isEnabled()) {
            log.warn("事件处理已禁用，无法发布事件: eventType={}, event={}", eventType, event);
            return;
        }
        
        if (eventPublisher == null) {
            log.warn("事件发布器未初始化，无法发布事件: eventType={}, event={}", eventType, event);
            return;
        }
        
        eventPublisher.publishEvent(event, eventType);
    }
    
    @Override
    public void publish(Object event, String eventType, long timestamp) {
        if (!eventConfig.isEnabled()) {
            log.warn("事件处理已禁用，无法发布事件: eventType={}, event={}, timestamp={}", eventType, event, timestamp);
            return;
        }
        
        if (eventPublisher == null) {
            log.warn("事件发布器未初始化，无法发布事件: eventType={}, event={}, timestamp={}", eventType, event, timestamp);
            return;
        }
        
        eventPublisher.publishEvent(event, eventType, timestamp);
    }
    
    @Override
    public void publishSync(Object event, String eventType) {
        if (!eventConfig.isEnabled()) {
            log.warn("事件处理已禁用，无法同步发布事件: eventType={}, event={}", eventType, event);
            return;
        }
        
        try {
            // 创建事件包装器并直接处理，实现同步处理
            EventDataWrapper eventWrapper = new EventDataWrapper();
            eventWrapper.setEventData(event);
            eventWrapper.setEventType(eventType);
            eventWrapper.setTimestamp(System.currentTimeMillis());
            
            log.debug("同步发布事件: eventType={}, event={}", eventType, event);
            eventService.handleEventSync(eventWrapper);
        } catch (Exception e) {
            log.error("同步发布事件失败: eventType={}, event={}", eventType, event, e);
        }
    }
}