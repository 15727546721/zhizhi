package cn.xu.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@Profile("!test") // 在测试环境中不加载此配置
@EnableElasticsearchRepositories(basePackages = "cn.xu.repository.read.elastic.repository")
public class ElasticsearchRepositoryConfig {
}