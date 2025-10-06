package cn.xu.infrastructure.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 事件配置类
 * 用于配置事件处理的相关参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.event")
public class EventConfig {
    
    /**
     * 是否启用事件处理
     */
    private boolean enabled = true;
    
    /**
     * 异步处理器线程池大小
     */
    private int asyncThreadPoolSize = 10;
    
    /**
     * Disruptor环形缓冲区大小
     */
    private int ringBufferSize = 1024;
    
    /**
     * Disruptor消费者线程数
     */
    private int consumerThreadCount = 4;
    
    /**
     * 是否启用事件监控
     */
    private boolean monitoringEnabled = true;
    
    /**
     * 事件处理失败重试次数
     */
    private int retryCount = 3;
    
    /**
     * 事件处理超时时间（毫秒）
     */
    private long timeoutMs = 5000;
}