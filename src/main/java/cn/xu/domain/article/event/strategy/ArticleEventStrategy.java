package cn.xu.domain.article.event.strategy;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.infrastructure.persistent.read.elastic.service.ArticleElasticService;

/**
 * 文章事件处理策略接口
 */
public interface ArticleEventStrategy {
    /**
     * 处理文章事件
     *
     * @param event        文章事件
     * @param indexService 索引服务
     */
    void handleEvent(ArticleEvent event, ArticleElasticService indexService);
} 