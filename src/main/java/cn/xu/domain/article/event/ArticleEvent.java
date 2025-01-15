package cn.xu.domain.article.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class ArticleEvent {
    private Long articleId;
    private String title;
    private String description;
    private String content;
    private Long userId;
    private EventType eventType;

    public enum EventType {
        CREATED,    // 创建事件
        UPDATED,    // 更新事件
        DELETED     // 删除事件
    }
} 