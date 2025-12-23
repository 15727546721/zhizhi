package cn.xu.config;

import cn.xu.common.annotation.ApiOperationLogAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * API操作日志自动配置
 */
@AutoConfiguration
public class ApiOperationLogAutoConfiguration {

    /**
     * 自定义API操作日志切面
     * @return
     */
    @Bean
    public ApiOperationLogAspect apiOperationLogAspect() {
        return new ApiOperationLogAspect();
    }
}