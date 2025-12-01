package cn.xu.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 事件监控服务
 * 用于监控事件处理情况，提供统计信息
 */
@Slf4j
@Service
public class EventMonitorService {
    
    /**
     * 事件处理统计
     * key: 事件类型
     * value: 处理次数
     */
    private final ConcurrentHashMap<String, AtomicLong> eventCountMap = new ConcurrentHashMap<>();
    
    /**
     * 事件处理失败统计
     * key: 事件类型
     * value: 失败次数
     */
    private final ConcurrentHashMap<String, AtomicLong> eventErrorCountMap = new ConcurrentHashMap<>();
    
    /**
     * 记录事件处理
     * @param eventType 事件类型
     */
    public void recordEventProcessed(String eventType) {
        eventCountMap.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * 记录事件处理失败
     * @param eventType 事件类型
     */
    public void recordEventError(String eventType) {
        eventErrorCountMap.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * 获取事件处理统计
     * @return 事件处理统计信息
     */
    public String getEventStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("事件处理统计:\n");
        
        for (String eventType : eventCountMap.keySet()) {
            long count = eventCountMap.get(eventType).get();
            long errorCount = eventErrorCountMap.getOrDefault(eventType, new AtomicLong(0)).get();
            sb.append(String.format("  %s: 处理%d次, 失败%d次\n", eventType, count, errorCount));
        }
        
        return sb.toString();
    }
    
    /**
     * 获取指定事件类型的处理次数
     * @param eventType 事件类型
     * @return 处理次数
     */
    public long getEventCount(String eventType) {
        return eventCountMap.getOrDefault(eventType, new AtomicLong(0)).get();
    }
    
    /**
     * 获取指定事件类型的失败次数
     * @param eventType 事件类型
     * @return 失败次数
     */
    public long getEventErrorCount(String eventType) {
        return eventErrorCountMap.getOrDefault(eventType, new AtomicLong(0)).get();
    }
}