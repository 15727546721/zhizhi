package cn.xu.domain.article.event.strategy;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.infrastructure.persistent.read.elastic.service.ArticleElasticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeleteArticleStrategy implements ArticleEventStrategy {

    @Autowired
    private ArticleElasticService elasticService;

    @Override
    public void handle(ArticleEvent event) {
        if (event.getType() != ArticleEvent.ArticleEventType.DELETED) return;
        log.info("处理文章删除事件: {}", event);
        elasticService.removeIndexedArticle(event.getArticleId());
    }
}