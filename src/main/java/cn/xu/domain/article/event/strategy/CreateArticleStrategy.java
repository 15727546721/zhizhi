package cn.xu.domain.article.event.strategy;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.infrastructure.persistent.read.elastic.service.ArticleElasticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("createArticleStrategy")
public class CreateArticleStrategy extends AbstractModifyArticleStrategy {

    @Override
    public void handleEvent(ArticleEvent event, ArticleElasticService indexService) {
        log.info("处理文章创建事件: {}", event);
        indexService.indexArticle(toArticleEntity(event));
    }
}