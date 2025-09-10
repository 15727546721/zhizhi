package cn.xu.domain.article.service;

import cn.xu.domain.article.service.impl.ElasticsearchArticleQueryStrategy;
import cn.xu.domain.article.service.impl.MySQLArticleQueryStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * 文章查询策略工厂
 * 根据配置决定使用哪种查询策略
 */
@Slf4j
@Component
public class ArticleQueryStrategyFactory {

    @Nullable
    private final ElasticsearchArticleQueryStrategy elasticsearchStrategy;
    
    private final MySQLArticleQueryStrategy mysqlStrategy;

    @Value("${app.article.query.strategy:mysql}")
    private String strategy;

    public ArticleQueryStrategyFactory(
            @Nullable ElasticsearchArticleQueryStrategy elasticsearchStrategy,
            MySQLArticleQueryStrategy mysqlStrategy) {
        this.elasticsearchStrategy = elasticsearchStrategy;
        this.mysqlStrategy = mysqlStrategy;
    }

    /**
     * 获取当前配置的文章查询策略
     * @return 文章查询策略实现
     */
    public ArticleQueryStrategy getCurrentStrategy() {
        if ("elasticsearch".equalsIgnoreCase(strategy) && isElasticsearchAvailable()) {
            log.info("使用Elasticsearch查询策略");
            return elasticsearchStrategy;
        } else {
            log.info("使用MySQL查询策略");
            return mysqlStrategy;
        }
    }

    /**
     * 检查Elasticsearch是否可用
     * @return Elasticsearch是否可用
     */
    private boolean isElasticsearchAvailable() {
        try {
            // 检查elasticsearchStrategy是否注入成功
            return elasticsearchStrategy != null;
        } catch (Exception e) {
            log.warn("Elasticsearch不可用，回退到MySQL查询", e);
            return false;
        }
    }
}