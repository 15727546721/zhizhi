package cn.xu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 事件配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.event")
public class EventConfig {
    
    /**
     * 是否启用事件处理
     */
    private boolean enabled = true;
}