package cn.xu.infrastructure.event.disruptor;

import lombok.Data;

/**
 * 事件数据包装类
 */
@Data
public class EventDataWrapper {
    /**
     * 事件数据
     */
    private Object eventData;
    
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 事件产生时间
     */
    private long timestamp;
}