package cn.xu.infrastructure.event.disruptor;

import cn.xu.infrastructure.event.DisruptorEventService;
import cn.xu.infrastructure.event.EventMonitorService;
import com.lmax.disruptor.WorkHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 事件消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer implements WorkHandler<EventDataWrapper> {
    
    private final DisruptorEventService disruptorEventService;
    private final EventMonitorService eventMonitorService;
    
    @Override
    public void onEvent(EventDataWrapper eventWrapper) throws Exception {
        try {
            log.debug("消费事件: eventType={}, eventData={}", eventWrapper.getEventType(), eventWrapper.getEventData());
            
            // 处理Disruptor事件
            disruptorEventService.handleEvent(eventWrapper);
        } catch (Exception e) {
            // 记录事件处理失败
            eventMonitorService.recordEventError(eventWrapper.getEventType());
            
            log.error("消费事件失败: eventType={}, eventData={}", eventWrapper.getEventType(), eventWrapper.getEventData(), e);
            throw e;
        }
    }
}