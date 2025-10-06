package cn.xu.infrastructure.event;

/**
 * 事件总线接口
 * 提供统一的事件发布接口，模拟MQ的功能
 */
public interface EventBus {
    
    /**
     * 发布事件
     * @param event 事件对象
     * @param eventType 事件类型
     */
    void publish(Object event, String eventType);
    
    /**
     * 发布事件（带时间戳）
     * @param event 事件对象
     * @param eventType 事件类型
     * @param timestamp 时间戳
     */
    void publish(Object event, String eventType, long timestamp);
    
    /**
     * 同步发布事件
     * @param event 事件对象
     * @param eventType 事件类型
     */
    void publishSync(Object event, String eventType);
}