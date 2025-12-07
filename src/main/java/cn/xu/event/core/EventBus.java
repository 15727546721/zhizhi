package cn.xu.event.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件总线
 * 
 * <p>应用内事件发布的统一接口，提供：
 * <ul>
 *   <li>统一的事件发布接口</li>
 *   <li>拦截器链支持（日志/监控/统计等）</li>
 *   <li>异常处理</li>
 * </ul>
 * 
 * <p>使用示例：
 * <pre>
 * eventBus.publish(new CommentEvent(userId, postId, commentId, CREATE));
 * </pre>
 *
 * 
 */
@Slf4j
@Component
public class EventBus {
    
    private final ApplicationEventPublisher publisher;
    private final List<EventInterceptor> interceptors;
    
    public EventBus(ApplicationEventPublisher publisher, List<EventInterceptor> interceptors) {
        this.publisher = publisher;
        this.interceptors = new CopyOnWriteArrayList<>();
        
        // 按优先级排序拦截器
        if (interceptors != null) {
            interceptors.stream()
                    .sorted(Comparator.comparingInt(EventInterceptor::getOrder))
                    .forEach(this.interceptors::add);
        }
        
        log.info("[EventBus] 初始化完成，已加载 {} 个拦截器", this.interceptors.size());
    }
    
    /**
     * 发布事件
     * 
     * @param event 事件对象（需继承BaseEvent）
     */
    public void publish(BaseEvent event) {
        if (event == null) {
            log.warn("[EventBus] 忽略空事件");
            return;
        }
        
        try {
            // 1. 前置拦截
            for (EventInterceptor interceptor : interceptors) {
                interceptor.before(event);
            }
            
            // 2. 发布事件
            publisher.publishEvent(event);
            
            log.debug("[EventBus] 事件已发布- {}", event);
            
            // 3. 后置拦截
            for (EventInterceptor interceptor : interceptors) {
                interceptor.after(event);
            }
            
        } catch (Exception e) {
            log.error("[EventBus] 事件发布失败 - {}", event, e);
            
            // 异常拦截
            for (EventInterceptor interceptor : interceptors) {
                try {
                    interceptor.onError(event, e);
                } catch (Exception ex) {
                    log.error("[EventBus] 拦截器onError执行失败", ex);
                }
            }
            
            throw e;
        }
    }
    
    /**
     * 注册拦截器
     */
    public void addInterceptor(EventInterceptor interceptor) {
        if (interceptor != null) {
            interceptors.add(interceptor);
            interceptors.sort(Comparator.comparingInt(EventInterceptor::getOrder));
        }
    }
}
