package cn.xu.domain.article.event.strategy;

import cn.xu.domain.article.event.ArticleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateArticleStrategy extends AbstractArticleStrategy {

    @Override
    public void handle(ArticleEvent event) {
        if (event.getType() != ArticleEvent.ArticleEventType.CREATED) return;
        log.info("处理文章创建事件: {}", event);
        
        // 检查Elasticsearch是否可用
        if (!isElasticsearchAvailable()) {
            return;
        }
        
        elasticService.indexArticle(toEntity(event));
    }
}