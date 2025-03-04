package cn.xu.domain.article.event;

import lombok.Getter;
import lombok.ToString;

/**
 * 文章更新事件
 */
@Getter
@ToString
public class ArticleUpdatedEvent extends ArticleEvent {
    
    public ArticleUpdatedEvent(Long articleId, String title, String description, String content, Long userId) {
        super(articleId, title, description, content, userId, EventType.UPDATED);
    }
} 