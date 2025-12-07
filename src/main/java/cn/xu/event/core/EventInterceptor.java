package cn.xu.event.core;

/**
 * 事件拦截器接口
 * 
 * <p>用于在事件发布前后执行通用逻辑：
 * <ul>
 *   <li>日志记录</li>
 *   <li>性能监控</li>
 *   <li>统计数据</li>
 * </ul>
 *
 * 
 */
public interface EventInterceptor {
    
    /**
     * 事件发布前执行
     * 
     * @param event 事件对象
     */
    default void before(BaseEvent event) {}
    
    /**
     * 事件发布后执行
     * 
     * @param event 事件对象
     */
    default void after(BaseEvent event) {}
    
    /**
     * 事件处理异常时执行
     * 
     * @param event 事件对象
     * @param ex 异常
     */
    default void onError(BaseEvent event, Exception ex) {}
    
    /**
     * 拦截器优先级（数值越小优先级越高）
     */
    default int getOrder() {
        return 0;
    }
}
