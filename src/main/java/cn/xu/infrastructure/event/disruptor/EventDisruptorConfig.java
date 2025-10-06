package cn.xu.infrastructure.event.disruptor;

import cn.xu.infrastructure.event.DisruptorEventService;
import cn.xu.infrastructure.event.EventConfig;
import cn.xu.infrastructure.event.EventMonitorService;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Disruptor事件配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class EventDisruptorConfig {

    private final EventConfig eventConfig;
    private final DisruptorEventService disruptorEventService; // 添加DisruptorEventService依赖
    
    /**
     * 创建事件工厂
     */
    @Bean
    public EventFactory<EventDataWrapper> eventFactory() {
        return EventDataWrapper::new;
    }
    
    /**
     * 创建Disruptor实例
     */
    @Bean
    public Disruptor<EventDataWrapper> disruptor(EventFactory<EventDataWrapper> eventFactory, EventMonitorService eventMonitorService) {
        if (!eventConfig.isEnabled()) {
            log.info("事件处理已禁用，不创建Disruptor实例");
            return null;
        }
        
        // 创建线程工厂
        ThreadFactory threadFactory = Executors.defaultThreadFactory();

        // 创建Disruptor
        Disruptor<EventDataWrapper> disruptor = new Disruptor<>(
                eventFactory,
                eventConfig.getRingBufferSize(), // 使用配置的缓冲区大小
                threadFactory,
                ProducerType.MULTI, // 支持多个生产者
                new BlockingWaitStrategy() // 等待策略
        );

        // 设置异常处理器
        disruptor.setDefaultExceptionHandler(new EventExceptionHandler());

        // 创建消费者
        EventConsumer[] consumers = new EventConsumer[eventConfig.getConsumerThreadCount()]; // 使用配置的消费者线程数
        for (int i = 0; i < eventConfig.getConsumerThreadCount(); i++) {
            consumers[i] = new EventConsumer(disruptorEventService, eventMonitorService);
        }

        // 设置工作池处理器
        disruptor.handleEventsWithWorkerPool(consumers);

        // 启动Disruptor
        disruptor.start();
        
        log.info("Disruptor事件处理已启动，缓冲区大小: {}, 消费者线程数: {}", 
                eventConfig.getRingBufferSize(), eventConfig.getConsumerThreadCount());

        return disruptor;
    }

    /**
     * 获取环形缓冲区
     */
    @Bean
    public RingBuffer<EventDataWrapper> ringBuffer(Disruptor<EventDataWrapper> disruptor) {
        return disruptor != null ? disruptor.getRingBuffer() : null;
    }

    /**
     * 创建事件生产者
     */
    @Bean
    public EventPublisher eventPublisher(RingBuffer<EventDataWrapper> ringBuffer) {
        return ringBuffer != null ? new EventPublisher(ringBuffer) : null;
    }
}