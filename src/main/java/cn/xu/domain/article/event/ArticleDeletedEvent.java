package cn.xu.domain.article.event;

import lombok.Getter;
import lombok.ToString;

/**
 * 文章删除事件
 */
@Getter
@ToString
public class ArticleDeletedEvent extends ArticleEvent {

    public ArticleDeletedEvent(Long articleId) {
        super(articleId, null, null, null, null, EventType.DELETED);
    }
} 