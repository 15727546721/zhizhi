package cn.xu.infrastructure.event;

import cn.xu.infrastructure.event.disruptor.EventDataWrapper;

import java.time.LocalDateTime;

/**
 * 事件工厂类
 * 提供统一的事件创建方法
 */
public class EventFactory {
    
    /**
     * 创建事件
     * @param eventData 事件数据
     * @param eventType 事件类型
     * @param <T> 事件数据类型
     * @return 事件包装对象
     */
    public static <T> EventDataWrapper createEvent(T eventData, String eventType) {
        EventDataWrapper eventWrapper = new EventDataWrapper();
        eventWrapper.setEventData(eventData);
        eventWrapper.setEventType(eventType);
        eventWrapper.setTimestamp(System.currentTimeMillis());
        return eventWrapper;
    }
    
    /**
     * 创建带时间戳的事件
     * @param eventData 事件数据
     * @param eventType 事件类型
     * @param timestamp 时间戳
     * @param <T> 事件数据类型
     * @return 事件包装对象
     */
    public static <T> EventDataWrapper createEvent(T eventData, String eventType, long timestamp) {
        EventDataWrapper eventWrapper = new EventDataWrapper();
        eventWrapper.setEventData(eventData);
        eventWrapper.setEventType(eventType);
        eventWrapper.setTimestamp(timestamp);
        return eventWrapper;
    }
    
    /**
     * 创建带时间的事件
     * @param eventData 事件数据
     * @param eventType 事件类型
     * @param time 时间
     * @param <T> 事件数据类型
     * @return 事件包装对象
     */
    public static <T> EventDataWrapper createEvent(T eventData, String eventType, LocalDateTime time) {
        EventDataWrapper eventWrapper = new EventDataWrapper();
        eventWrapper.setEventData(eventData);
        eventWrapper.setEventType(eventType);
        eventWrapper.setTimestamp(time.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        return eventWrapper;
    }
}