package cn.xu.domain.article.event.factory;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.article.event.strategy.ArticleEventStrategy;
import cn.xu.domain.article.event.strategy.CreateArticleStrategy;
import cn.xu.domain.article.event.strategy.DeleteArticleStrategy;
import cn.xu.domain.article.event.strategy.UpdateArticleStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 文章事件策略工厂
 * 负责创建和管理不同类型的事件处理策略
 */
@Component
@RequiredArgsConstructor
public class ArticleEventStrategyFactory {
    
    private final Map<ArticleEvent.EventType, ArticleEventStrategy> strategies = new HashMap<>();
    private final CreateArticleStrategy createArticleStrategy;
    private final UpdateArticleStrategy updateArticleStrategy;
    private final DeleteArticleStrategy deleteArticleStrategy;
    
    @PostConstruct
    public void init() {
        strategies.put(ArticleEvent.EventType.CREATED, createArticleStrategy);
        strategies.put(ArticleEvent.EventType.UPDATED, updateArticleStrategy);
        strategies.put(ArticleEvent.EventType.DELETED, deleteArticleStrategy);
    }
    
    /**
     * 获取事件处理策略
     * @param eventType 事件类型
     * @return 对应的处理策略
     */
    public ArticleEventStrategy getStrategy(ArticleEvent.EventType eventType) {
        ArticleEventStrategy strategy = strategies.get(eventType);
        if (strategy == null) {
            throw new IllegalArgumentException("未找到对应的事件处理策略: " + eventType);
        }
        return strategy;
    }
} 