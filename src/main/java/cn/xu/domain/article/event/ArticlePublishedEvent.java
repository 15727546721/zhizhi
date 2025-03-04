package cn.xu.domain.article.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ArticlePublishedEvent extends ApplicationEvent {
    private final Long articleId;
    
    public ArticlePublishedEvent(Long articleId) {
        super(articleId);
        this.articleId = articleId;
    }
} 