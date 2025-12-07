package cn.xu.config;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import java.time.Duration;

/**
 * Elasticsearch配置类
 * 配置ES客户端连接信息
 * 
 *
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@Profile("!test") // 在测试环境中不加载此配置
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:127.0.0.1:9200}")
    private String elasticsearchUrl;
    
    @Value("${spring.elasticsearch.connection-timeout:5s}")
    private String connectionTimeout;
    
    @Value("${spring.elasticsearch.read-timeout:30s}")
    private String readTimeout;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        log.info("初始化Elasticsearch客户端: uris={}, connectionTimeout={}, readTimeout={}", 
                elasticsearchUrl, connectionTimeout, readTimeout);
        
        try {
            // 解析超时时间
            Duration connectTimeoutDuration = parseDuration(connectionTimeout);
            Duration socketTimeoutDuration = parseDuration(readTimeout);
            
            ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                    .connectedTo(elasticsearchUrl)
                    .withConnectTimeout(connectTimeoutDuration)
                    .withSocketTimeout(socketTimeoutDuration)
                    .build();
            
            RestHighLevelClient client = RestClients.create(clientConfiguration).rest();
            log.info("Elasticsearch客户端初始化成功");
            return client;
        } catch (Exception e) {
            log.error("Elasticsearch客户端初始化失败: uris={}", elasticsearchUrl, e);
            throw e;
        }
    }

    @Bean
    public ElasticsearchOperations elasticsearchOperations() {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }
    
    /**
     * 解析Duration字符串（如"5s", "30s"）
     */
    private Duration parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return Duration.ofSeconds(5);
        }
        
        try {
            // 移除空格并转换为小写
            String clean = durationStr.trim().toLowerCase();
            
            if (clean.endsWith("s")) {
                long seconds = Long.parseLong(clean.substring(0, clean.length() - 1));
                return Duration.ofSeconds(seconds);
            } else if (clean.endsWith("ms")) {
                long millis = Long.parseLong(clean.substring(0, clean.length() - 2));
                return Duration.ofMillis(millis);
            } else if (clean.endsWith("m")) {
                long minutes = Long.parseLong(clean.substring(0, clean.length() - 1));
                return Duration.ofMinutes(minutes);
            } else {
                // 默认当作秒处理
                long seconds = Long.parseLong(clean);
                return Duration.ofSeconds(seconds);
            }
        } catch (Exception e) {
            log.warn("解析Duration失败: {}, 使用默认值5s", durationStr, e);
            return Duration.ofSeconds(5);
        }
    }
}