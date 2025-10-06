package cn.xu.infrastructure.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Disruptor事件监听注解
 * 用于标识事件监听方法，模拟MQ的订阅机制
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DisruptorListener {
    
    /**
     * 监听的事件类型
     */
    String eventType();
    
    /**
     * 是否异步处理
     */
    boolean async() default true;
    
    /**
     * 事件处理优先级，数值越小优先级越高
     */
    int priority() default 0;
    
    /**
     * 是否启用该监听器
     */
    boolean enabled() default true;
}