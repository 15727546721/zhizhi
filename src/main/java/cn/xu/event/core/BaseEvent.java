package cn.xu.event.core;

import lombok.Getter;

import java.util.UUID;

/**
 * 事件基类
 * 
 * <p>所有业务事件需继承此类，提供统一的事件元数据
 *
 *
 */
@Getter
public abstract class BaseEvent {
    
    /** 事件唯一ID */
    private final String eventId;
    
    /** 事件发生时间戳 */
    private final long timestamp;
    
    /** 操作者ID */
    private final Long operatorId;
    
    /** 事件操作类型 */
    private final EventAction action;
    
    /**
     * 事件操作枚举
     */
    public enum EventAction {
        /** 创建 */
        CREATE,
        /** 更新 */
        UPDATE,
        /** 删除 */
        DELETE
    }
    
    protected BaseEvent(Long operatorId, EventAction action) {
        this.eventId = UUID.randomUUID().toString().replace("-", "");
        this.timestamp = System.currentTimeMillis();
        this.operatorId = operatorId;
        this.action = action;
    }
    
    /**
     * 获取事件类型名称
     */
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    public String toString() {
        return String.format("%s{eventId='%s', operatorId=%d, action=%s}", 
                getEventType(), eventId, operatorId, action);
    }
}
