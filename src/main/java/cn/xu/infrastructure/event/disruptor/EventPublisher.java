package cn.xu.infrastructure.event.disruptor;

import com.lmax.disruptor.RingBuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 事件发布者
 */
@Slf4j
@RequiredArgsConstructor
public class EventPublisher {
    
    private final RingBuffer<EventDataWrapper> ringBuffer;
    
    /**
     * 发布事件
     * @param eventData 事件数据
     * @param eventType 事件类型
     */
    public void publishEvent(Object eventData, String eventType) {
        // 检查ringBuffer是否为空
        if (ringBuffer == null) {
            log.warn("事件发布器未初始化，无法发布事件: eventType={}, eventData={}", eventType, eventData);
            return;
        }
        
        try {
            // 获取下一个序列号
            long sequence = ringBuffer.next();
            
            try {
                // 获取对应的事件对象
                EventDataWrapper event = ringBuffer.get(sequence);
                
                // 设置事件数据
                event.setEventData(eventData);
                event.setEventType(eventType);
                event.setTimestamp(System.currentTimeMillis());
                
                log.debug("发布事件: eventType={}, eventData={}", eventType, eventData);
            } finally {
                // 发布事件
                ringBuffer.publish(sequence);
            }
        } catch (Exception e) {
            log.error("发布事件失败: eventType={}, eventData={}", eventType, eventData, e);
        }
    }
    
    /**
     * 发布事件的重载方法，支持指定时间戳
     * @param eventData 事件数据
     * @param eventType 事件类型
     * @param timestamp 时间戳
     */
    public void publishEvent(Object eventData, String eventType, long timestamp) {
        // 检查ringBuffer是否为空
        if (ringBuffer == null) {
            log.warn("事件发布器未初始化，无法发布事件: eventType={}, eventData={}, timestamp={}", eventType, eventData, timestamp);
            return;
        }
        
        try {
            // 获取下一个序列号
            long sequence = ringBuffer.next();
            
            try {
                // 获取对应的事件对象
                EventDataWrapper event = ringBuffer.get(sequence);
                
                // 设置事件数据
                event.setEventData(eventData);
                event.setEventType(eventType);
                event.setTimestamp(timestamp);
                
                log.debug("发布事件: eventType={}, eventData={}, timestamp={}", eventType, eventData, timestamp);
            } finally {
                // 发布事件
                ringBuffer.publish(sequence);
            }
        } catch (Exception e) {
            log.error("发布事件失败: eventType={}, eventData={}, timestamp={}", eventType, eventData, timestamp, e);
        }
    }
}