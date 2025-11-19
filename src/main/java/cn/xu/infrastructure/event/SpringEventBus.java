package cn.xu.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 基于Spring Event的事件总线实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringEventBus implements EventBus {
    
    private final ApplicationEventPublisher eventPublisher;
    private final EventConfig eventConfig;
    
    @Override
    public void publish(Object event, String eventType) {
        if (!eventConfig.isEnabled()) {
            log.warn("事件处理已禁用，无法发布事件: eventType={}, event={}", eventType, event);
            return;
        }
        
        log.debug("发布事件: eventType={}, event={}", eventType, event);
        
        // 包装事件数据
        GenericEvent genericEvent = new GenericEvent(event, eventType, System.currentTimeMillis());
        eventPublisher.publishEvent(genericEvent);
    }
    
    @Override
    public void publish(Object event, String eventType, long timestamp) {
        if (!eventConfig.isEnabled()) {
            log.warn("事件处理已禁用，无法发布事件: eventType={}, event={}, timestamp={}", 
                    eventType, event, timestamp);
            return;
        }
        
        log.debug("发布事件: eventType={}, event={}, timestamp={}", eventType, event, timestamp);
        
        // 包装事件数据
        GenericEvent genericEvent = new GenericEvent(event, eventType, timestamp);
        eventPublisher.publishEvent(genericEvent);
    }
    
    @Override
    public void publishSync(Object event, String eventType) {
        // Spring Event默认是同步发布，异步执行由@Async控制
        // 这里直接调用publish即可
        publish(event, eventType);
    }
    
    /**
     * 通用事件包装类
     * 用于包装各种类型的事件数据
     */
    public static class GenericEvent {
        private final Object eventData;
        private final String eventType;
        private final long timestamp;
        
        public GenericEvent(Object eventData, String eventType, long timestamp) {
            this.eventData = eventData;
            this.eventType = eventType;
            this.timestamp = timestamp;
        }
        
        public Object getEventData() {
            return eventData;
        }
        
        public String getEventType() {
            return eventType;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return "GenericEvent{" +
                    "eventType='" + eventType + '\'' +
                    ", timestamp=" + timestamp +
                    ", eventData=" + eventData +
                    '}';
        }
    }
}
