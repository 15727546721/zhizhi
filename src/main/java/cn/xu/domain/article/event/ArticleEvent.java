package cn.xu.domain.article.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleEvent {

    private Long articleId;
    private String title;
    private String description;
    private String content;
    private Long userId;

    /**
     * 事件类型
     */
    private ArticleEventType type;

    public enum ArticleEventType {
        CREATED,
        UPDATED,
        DELETED
    }
}