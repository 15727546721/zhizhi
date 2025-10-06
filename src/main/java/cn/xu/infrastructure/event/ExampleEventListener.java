package cn.xu.infrastructure.event;

import cn.xu.infrastructure.event.annotation.DisruptorListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 示例事件监听器
 * 演示如何使用Disruptor事件机制
 */
@Slf4j
@Component
public class ExampleEventListener {
    
    /**
     * 处理测试事件
     */
    @DisruptorListener(eventType = "TestEvent", priority = 1)
    public void handleTestEvent(Map<String, Object> event) {
        log.info("处理测试事件: {}", event);
        // 模拟处理耗时
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 处理用户事件
     */
    @DisruptorListener(eventType = "UserEvent", priority = 2)
    public void handleUserEvent(Map<String, Object> event) {
        log.info("处理用户事件: {}", event);
        // 模拟处理耗时
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 处理帖子事件（同步处理）
     */
    @DisruptorListener(eventType = "PostEvent", async = false, priority = 0)
    public void handlePostEvent(Map<String, Object> event) {
        log.info("同步处理帖子事件: {}", event);
        // 同步处理，不需要模拟耗时
    }
    
    /**
     * 处理同步测试事件
     */
    @DisruptorListener(eventType = "SyncTestEvent", async = false, priority = 0)
    public void handleSyncTestEvent(Map<String, Object> event) {
        log.info("同步处理测试事件: {}", event);
    }
}