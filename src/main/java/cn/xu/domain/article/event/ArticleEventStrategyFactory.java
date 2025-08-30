package cn.xu.domain.article.event;

import cn.xu.domain.article.event.strategy.ArticleEventStrategy;
import cn.xu.domain.article.event.strategy.CreateArticleStrategy;
import cn.xu.domain.article.event.strategy.DeleteArticleStrategy;
import cn.xu.domain.article.event.strategy.UpdateArticleStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ArticleEventStrategyFactory {

    private final CreateArticleStrategy createStrategy;
    private final UpdateArticleStrategy updateStrategy;
    private final DeleteArticleStrategy deleteStrategy;

    private final Map<ArticleEvent.ArticleEventType, ArticleEventStrategy> strategyMap
            = new EnumMap<>(ArticleEvent.ArticleEventType.class);

    @PostConstruct
    public void init() {
        strategyMap.put(ArticleEvent.ArticleEventType.CREATED, createStrategy);
        strategyMap.put(ArticleEvent.ArticleEventType.UPDATED, updateStrategy);
        strategyMap.put(ArticleEvent.ArticleEventType.DELETED, deleteStrategy);
    }

    public ArticleEventStrategy getStrategy(ArticleEvent.ArticleEventType type) {
        return strategyMap.get(type);
    }
}
