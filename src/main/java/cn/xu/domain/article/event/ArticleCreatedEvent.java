package cn.xu.domain.article.event;

import lombok.Getter;
import lombok.ToString;

/**
 * 文章创建事件
 */
@Getter
@ToString
public class ArticleCreatedEvent extends ArticleEvent {

    public ArticleCreatedEvent(Long articleId, String title, String description, String content, Long userId) {
        super(articleId, title, description, content, userId, EventType.CREATED);
    }
} 