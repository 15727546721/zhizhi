package cn.xu.elasticsearch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import java.time.Duration;

/**
 * Elasticsearch 配置类
 * <p>Spring Boot 3.x + Elasticsearch 7.17.x</p>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:127.0.0.1:9200}")
    private String elasticsearchUrl;
    
    @Value("${spring.elasticsearch.connection-timeout:5s}")
    private String connectionTimeout;
    
    @Value("${spring.elasticsearch.read-timeout:30s}")
    private String readTimeout;

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("=== ElasticsearchConfig 已加载 ===");
        log.info("ES URL: {}", elasticsearchUrl);
        log.info("连接超时: {}", connectionTimeout);
        log.info("读取超时: {}", readTimeout);
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        log.info("初始化 Elasticsearch 客户端: uris={}", elasticsearchUrl);
        
        Duration connectTimeoutDuration = parseDuration(connectionTimeout);
        Duration socketTimeoutDuration = parseDuration(readTimeout);
        
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUrl)
                .withConnectTimeout(connectTimeoutDuration)
                .withSocketTimeout(socketTimeoutDuration)
                .build();
    }
    
    /**
     * 创建我们自己的 ElasticsearchOperations 封装类
     * 注意：Spring Data Elasticsearch 5.x 会自动创建 ElasticsearchOperations Bean
     * 我们直接注入使用即可
     */
    @Bean
    public cn.xu.elasticsearch.core.ElasticsearchOperations customElasticsearchOperations(
            org.springframework.data.elasticsearch.core.ElasticsearchOperations elasticsearchTemplate) {
        log.info("创建自定义 ElasticsearchOperations 封装类");
        return new cn.xu.elasticsearch.core.ElasticsearchOperations(elasticsearchTemplate);
    }
    
    private Duration parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return Duration.ofSeconds(5);
        }
        try {
            String clean = durationStr.trim().toLowerCase();
            if (clean.endsWith("s")) {
                return Duration.ofSeconds(Long.parseLong(clean.substring(0, clean.length() - 1)));
            } else if (clean.endsWith("ms")) {
                return Duration.ofMillis(Long.parseLong(clean.substring(0, clean.length() - 2)));
            }
            return Duration.ofSeconds(Long.parseLong(clean));
        } catch (Exception e) {
            log.warn("解析 Duration 失败: {}, 使用默认值 5s", durationStr);
            return Duration.ofSeconds(5);
        }
    }
}
