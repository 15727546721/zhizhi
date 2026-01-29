package cn.xu.config;

import cn.xu.support.util.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 值对象配置类
 * 用于初始化值对象中的静态依赖
 * 注：CommentContent已移除，敏感词过滤改为Service层实现
 */
/**
 * 值对象配置类
 * 
 * 
 */
@Configuration
public class ValueObjectConfig {

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @PostConstruct
    public void initValueObjects() {
        // 值对象已简化，敏感词过滤器可通过@Autowired在Service中使用
    }
}