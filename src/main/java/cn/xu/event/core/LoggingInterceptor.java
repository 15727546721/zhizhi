package cn.xu.event.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 日志监控拦截器
 * 
 * <p>功能：
 * <ul>
 *   <li>记录事件发布日志</li>
 *   <li>统计各类事件数量</li>
 *   <li>记录异常信息</li>
 * </ul>
 *
 * 
 */
@Slf4j
@Component
public class LoggingInterceptor implements EventInterceptor {
    
    /** 事件计数 */
    private final ConcurrentHashMap<String, AtomicLong> eventCounts = new ConcurrentHashMap<>();
    
    /** 错误计数 */
    private final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    
    @Override
    public void before(BaseEvent event) {
        log.info("[Event] 发布 {} - operator:{}, action:{}", 
                event.getEventType(), event.getOperatorId(), event.getAction());
    }
    
    @Override
    public void after(BaseEvent event) {
        // 统计计数
        eventCounts.computeIfAbsent(event.getEventType(), k -> new AtomicLong(0))
                .incrementAndGet();
    }
    
    @Override
    public void onError(BaseEvent event, Exception ex) {
        log.error("[Event] 处理失败 {} - {}", event.getEventType(), ex.getMessage());
        errorCounts.computeIfAbsent(event.getEventType(), k -> new AtomicLong(0))
                .incrementAndGet();
    }
    
    @Override
    public int getOrder() {
        return 0; // 最高优先级
    }
    
    /**
     * 获取事件统计
     */
    public String getStatistics() {
        StringBuilder sb = new StringBuilder("事件统计:\n");
        eventCounts.forEach((type, count) -> {
            long errors = errorCounts.getOrDefault(type, new AtomicLong(0)).get();
            sb.append(String.format("  %s: %d次  失败%d次\n", type, count.get(), errors));
        });
        return sb.toString();
    }
    
    /**
     * 获取指定事件的发布次数
     */
    public long getCount(String eventType) {
        return eventCounts.getOrDefault(eventType, new AtomicLong(0)).get();
    }
}
