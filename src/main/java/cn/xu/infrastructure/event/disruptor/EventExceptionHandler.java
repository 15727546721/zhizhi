package cn.xu.infrastructure.event.disruptor;

import cn.xu.infrastructure.event.EventMonitorService;
import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 事件异常处理器
 */
@Slf4j
@Component
public class EventExceptionHandler implements ExceptionHandler<EventDataWrapper> {
    
    @Autowired
    private EventMonitorService eventMonitorService;
    
    @Override
    public void handleEventException(Throwable ex, long sequence, EventDataWrapper event) {
        log.error("处理事件时发生异常: sequence={}, event={}", sequence, event, ex);
        
        // 记录事件处理失败
        if (event != null && event.getEventType() != null) {
            eventMonitorService.recordEventError(event.getEventType());
        }
    }
    
    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("启动Disruptor时发生异常", ex);
    }
    
    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("关闭Disruptor时发生异常", ex);
    }
}