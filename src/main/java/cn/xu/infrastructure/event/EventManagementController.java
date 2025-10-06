package cn.xu.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 事件管理控制器
 * 提供事件处理情况的监控和管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventManagementController {
    
    private final EventMonitorService eventMonitorService;
    private final DisruptorEventHandlerRegistry eventHandlerRegistry;
    
    /**
     * 获取事件处理统计信息
     */
    @GetMapping("/statistics")
    public String getEventStatistics() {
        return eventMonitorService.getEventStatistics();
    }
    
    /**
     * 获取事件处理器注册信息
     */
    @GetMapping("/handlers")
    public String getEventHandlers() {
        StringBuilder sb = new StringBuilder();
        sb.append("事件处理器注册信息:\n");
        
        // 通过反射获取私有字段
        try {
            java.lang.reflect.Field eventHandlerMapField = DisruptorEventHandlerRegistry.class.getDeclaredField("eventHandlerMap");
            eventHandlerMapField.setAccessible(true);
            Map<String, ?> eventHandlerMap = (Map<String, ?>) eventHandlerMapField.get(eventHandlerRegistry);
            
            for (Map.Entry<String, ?> entry : eventHandlerMap.entrySet()) {
                String eventType = entry.getKey();
                Object handlers = entry.getValue();
                sb.append(String.format("  %s: %s个处理器\n", eventType, 
                    handlers instanceof java.util.List ? ((java.util.List<?>) handlers).size() : "未知"));
            }
        } catch (Exception e) {
            log.error("获取事件处理器信息失败", e);
            sb.append("获取事件处理器信息失败: ").append(e.getMessage());
        }
        
        return sb.toString();
    }
    
    /**
     * 获取指定事件类型的处理统计
     */
    @GetMapping("/count")
    public String getEventCount(String eventType) {
        if (eventType == null || eventType.isEmpty()) {
            return "事件类型不能为空";
        }
        
        long count = eventMonitorService.getEventCount(eventType);
        long errorCount = eventMonitorService.getEventErrorCount(eventType);
        
        return String.format("事件类型 '%s' 的处理统计: 处理%d次, 失败%d次", eventType, count, errorCount);
    }
}