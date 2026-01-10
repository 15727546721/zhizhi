package cn.xu.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch Repository配置
 * 
 *
 */
@Configuration
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@Profile("!test")
@EnableElasticsearchRepositories(basePackages = "cn.xu.repository.read.elastic.repository")
public class ElasticsearchRepositoryConfig {
}