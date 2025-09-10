package cn.xu.domain.article.event.strategy;


import cn.xu.domain.article.event.ArticleEvent;

public interface ArticleEventStrategy {
    void handle(ArticleEvent event);
}