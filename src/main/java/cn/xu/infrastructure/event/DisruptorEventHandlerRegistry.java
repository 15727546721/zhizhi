package cn.xu.infrastructure.event;

import cn.xu.infrastructure.event.annotation.DisruptorListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Disruptor事件处理器注册中心
 * 扫描并注册所有带@DisruptorListener注解的方法
 */
@Slf4j
@Component
public class DisruptorEventHandlerRegistry implements ApplicationListener<ContextRefreshedEvent> {
    
    /**
     * 事件处理器映射
     * key: 事件类型
     * value: 处理器方法列表
     */
    private final Map<String, List<EventHandlerMethod>> eventHandlerMap = new ConcurrentHashMap<>();
    
    private ApplicationContext applicationContext;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (applicationContext == null) {
            applicationContext = event.getApplicationContext();
            registerEventHandlers();
        }
    }
    
    /**
     * 注册事件处理器
     */
    private void registerEventHandlers() {
        log.info("开始注册Disruptor事件处理器");
        
        // 获取所有Spring管理的Bean
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            // 检查Bean是否带有Component注解
            if (bean.getClass().isAnnotationPresent(org.springframework.stereotype.Component.class) ||
                bean.getClass().isAnnotationPresent(org.springframework.stereotype.Service.class) ||
                bean.getClass().isAnnotationPresent(org.springframework.stereotype.Repository.class)) {
                Method[] methods = bean.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    DisruptorListener listener = method.getAnnotation(DisruptorListener.class);
                    if (listener != null && listener.enabled()) {
                        String eventType = listener.eventType();
                        eventHandlerMap.computeIfAbsent(eventType, k -> new ArrayList<>())
                                .add(new EventHandlerMethod(bean, method, listener.async(), listener.priority()));
                        log.info("注册事件处理器: eventType={}, bean={}, method={}, priority={}, async={}", 
                                eventType, bean.getClass().getSimpleName(), method.getName(), listener.priority(), listener.async());
                    }
                }
            }
        }
        
        // 对所有事件处理器按优先级排序
        eventHandlerMap.values().forEach(handlers -> 
            handlers.sort(Comparator.comparingInt(EventHandlerMethod::getPriority)));
        
        log.info("Disruptor事件处理器注册完成，共注册{}种事件类型，{}个处理器", eventHandlerMap.size(), 
                eventHandlerMap.values().stream().mapToInt(List::size).sum());
    }
    
    /**
     * 获取事件处理器
     */
    public List<EventHandlerMethod> getEventHandlers(String eventType) {
        return eventHandlerMap.getOrDefault(eventType, Collections.emptyList());
    }
    
    /**
     * 事件处理器方法封装类
     */
    public static class EventHandlerMethod {
        private final Object bean;
        private final Method method;
        private final boolean async;
        private final int priority;
        
        public EventHandlerMethod(Object bean, Method method, boolean async, int priority) {
            this.bean = bean;
            this.method = method;
            this.async = async;
            this.priority = priority;
        }
        
        public Object getBean() {
            return bean;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public boolean isAsync() {
            return async;
        }
        
        public int getPriority() {
            return priority;
        }
    }
}