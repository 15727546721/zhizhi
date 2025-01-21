package cn.xu.domain.article.service.impl;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.article.event.ArticleEventWrapper;
import cn.xu.domain.article.event.factory.ArticleEventStrategyFactory;
import cn.xu.domain.article.event.strategy.ArticleEventStrategy;
import com.lmax.disruptor.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文章领域事件处理者
 * 使用策略模式处理不同类型的事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleDomainEventHandler implements EventHandler<ArticleEventWrapper> {
    
    private final ArticleEventStrategyFactory strategyFactory;
    private final ArticleIndexService articleIndexService;
    
    @Override
    public void onEvent(ArticleEventWrapper eventWrapper, long sequence, boolean endOfBatch) {
        ArticleEvent event = eventWrapper.getEvent();
        try {
            ArticleEventStrategy strategy = strategyFactory.getStrategy(event.getEventType());
            strategy.handleEvent(event, articleIndexService);
        } catch (Exception e) {
            log.error("处理文章领域事件失败: {}", event, e);
        }
    }
} 