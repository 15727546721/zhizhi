package cn.xu.domain.article.event.strategy;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.article.model.entity.ArticleEntity;

public abstract class AbstractModifyArticleStrategy implements ArticleEventStrategy {

    protected ArticleEntity toArticleEntity(ArticleEvent event) {
        return ArticleEntity.builder()
                .id(event.getArticleId())
                .title(event.getTitle())
                .description(event.getDescription())
                .content(event.getContent())
                .userId(event.getUserId())
                .build();
    }
}

