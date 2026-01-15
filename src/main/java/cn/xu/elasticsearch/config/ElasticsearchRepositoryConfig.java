package cn.xu.elasticsearch.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch Repository 配置
 * <p>启用 Elasticsearch 仓库扫描</p>
 */
@Configuration
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@EnableElasticsearchRepositories(basePackages = "cn.xu.elasticsearch.repository")
public class ElasticsearchRepositoryConfig {
}
