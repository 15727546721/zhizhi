package cn.xu.domain.article.event.strategy;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.service.article.ArticleIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("createArticleStrategy")
public class CreateArticleStrategy implements ArticleEventStrategy {
    
    @Override
    public void handleEvent(ArticleEvent event, ArticleIndexService indexService) {
        log.info("处理文章创建事件: {}", event);
        ArticleEntity article = ArticleEntity.builder()
                .id(event.getArticleId())
                .title(event.getTitle())
                .description(event.getDescription())
                .content(event.getContent())
                .userId(event.getUserId())
                .build();
        indexService.addToIndex(article);
    }
} 