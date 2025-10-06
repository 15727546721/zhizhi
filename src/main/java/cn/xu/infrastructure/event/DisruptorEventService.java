package cn.xu.infrastructure.event;

import cn.xu.infrastructure.event.disruptor.EventDataWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Disruptor事件处理服务
 * 处理从Disruptor队列中消费的事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisruptorEventService {
    
    private final DisruptorEventHandlerRegistry eventHandlerRegistry;
    private final EventMonitorService eventMonitorService;
    private final EventConfig eventConfig;
    
    private ExecutorService asyncExecutor;
    
    @PostConstruct
    public void init() {
        // 根据配置初始化异步处理器线程池
        asyncExecutor = Executors.newFixedThreadPool(eventConfig.getAsyncThreadPoolSize());
        log.info("初始化事件处理服务，异步线程池大小: {}", eventConfig.getAsyncThreadPoolSize());
    }
    
    /**
     * 处理事件
     * @param eventWrapper 事件包装对象
     */
    public void handleEvent(EventDataWrapper eventWrapper) {
        try {
            String eventType = eventWrapper.getEventType();
            Object eventData = eventWrapper.getEventData();
            
            log.debug("开始处理事件: eventType={}, eventData={}", eventType, eventData);
            
            // 记录事件处理
            eventMonitorService.recordEventProcessed(eventType);
            
            // 获取事件处理器列表
            List<DisruptorEventHandlerRegistry.EventHandlerMethod> handlerMethods = 
                eventHandlerRegistry.getEventHandlers(eventType);
            
            if (!handlerMethods.isEmpty()) {
                // 遍历所有处理器
                for (DisruptorEventHandlerRegistry.EventHandlerMethod handlerMethod : handlerMethods) {
                    if (handlerMethod.isAsync()) {
                        // 异步处理
                        asyncExecutor.submit(() -> invokeHandler(handlerMethod, eventData, eventType));
                    } else {
                        // 同步处理
                        invokeHandler(handlerMethod, eventData, eventType);
                    }
                }
            } else {
                log.warn("未找到事件处理器: eventType={}", eventType);
            }
        } catch (Exception e) {
            log.error("处理事件失败: eventWrapper={}", eventWrapper, e);
        }
    }
    
    /**
     * 同步处理事件
     * @param eventWrapper 事件包装对象
     */
    public void handleEventSync(EventDataWrapper eventWrapper) {
        try {
            String eventType = eventWrapper.getEventType();
            Object eventData = eventWrapper.getEventData();
            
            log.debug("开始同步处理事件: eventType={}, eventData={}", eventType, eventData);
            
            // 记录事件处理
            eventMonitorService.recordEventProcessed(eventType);
            
            // 获取事件处理器列表
            List<DisruptorEventHandlerRegistry.EventHandlerMethod> handlerMethods = 
                eventHandlerRegistry.getEventHandlers(eventType);
            
            if (!handlerMethods.isEmpty()) {
                // 遍历所有处理器并同步执行
                for (DisruptorEventHandlerRegistry.EventHandlerMethod handlerMethod : handlerMethods) {
                    invokeHandler(handlerMethod, eventData, eventType);
                }
            } else {
                log.warn("未找到事件处理器: eventType={}", eventType);
            }
        } catch (Exception e) {
            log.error("同步处理事件失败: eventWrapper={}", eventWrapper, e);
        }
    }
    
    /**
     * 调用事件处理器方法
     */
    private void invokeHandler(DisruptorEventHandlerRegistry.EventHandlerMethod handlerMethod, Object eventData, String eventType) {
        try {
            Object bean = handlerMethod.getBean();
            Method method = handlerMethod.getMethod();
            
            // 设置方法可访问
            method.setAccessible(true);
            
            // 根据方法参数数量调用方法
            if (method.getParameterCount() == 0) {
                method.invoke(bean);
            } else if (method.getParameterCount() == 1) {
                method.invoke(bean, eventData);
            } else {
                log.warn("不支持的事件处理器方法签名: bean={}, method={}, parameterCount={}", 
                        bean.getClass().getSimpleName(), method.getName(), method.getParameterCount());
            }
            
            log.debug("事件处理完成: bean={}, method={}", bean.getClass().getSimpleName(), method.getName());
        } catch (Exception e) {
            // 记录事件处理失败
            eventMonitorService.recordEventError(eventType);
            
            log.error("调用事件处理器失败: bean={}, method={}", 
                handlerMethod.getBean().getClass().getSimpleName(), 
                handlerMethod.getMethod().getName(), e);
        }
    }
    
    /**
     * 关闭异步处理器线程池
     */
    public void shutdown() {
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("事件处理服务异步线程池已关闭");
        }
    }
}