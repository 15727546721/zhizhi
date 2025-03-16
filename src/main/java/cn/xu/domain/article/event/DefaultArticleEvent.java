package cn.xu.domain.article.event;

import lombok.NoArgsConstructor;

/**
 * 默认文章事件实现
 * 用于Disruptor事件工厂创建初始事件对象
 */
@NoArgsConstructor
public class DefaultArticleEvent extends ArticleEvent {

    @Override
    public EventType getEventType() {
        return super.getEventType();
    }
} 