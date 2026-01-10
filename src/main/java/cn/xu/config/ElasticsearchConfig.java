package cn.xu.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import java.time.Duration;

/**
 * Elasticsearch配置类（Spring Boot 3.x + ES 7.x 兼容版本）
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@Profile("!test")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:127.0.0.1:9200}")
    private String elasticsearchUrl;
    
    @Value("${spring.elasticsearch.connection-timeout:5s}")
    private String connectionTimeout;
    
    @Value("${spring.elasticsearch.read-timeout:30s}")
    private String readTimeout;

    @Override
    public ClientConfiguration clientConfiguration() {
        log.info("初始化Elasticsearch客户端: uris={}", elasticsearchUrl);
        
        Duration connectTimeoutDuration = parseDuration(connectionTimeout);
        Duration socketTimeoutDuration = parseDuration(readTimeout);
        
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUrl)
                .withConnectTimeout(connectTimeoutDuration)
                .withSocketTimeout(socketTimeoutDuration)
                .build();
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
            log.warn("解析Duration失败: {}, 使用默认值5s", durationStr);
            return Duration.ofSeconds(5);
        }
    }
}
