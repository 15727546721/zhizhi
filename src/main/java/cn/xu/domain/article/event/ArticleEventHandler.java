package cn.xu.domain.article.event;

import cn.xu.domain.article.event.factory.ArticleEventStrategyFactory;
import cn.xu.domain.article.event.strategy.ArticleEventStrategy;
import cn.xu.domain.article.service.impl.ArticleIndexService;
import com.lmax.disruptor.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文章领域事件处理者 (事件消息消费者)
 * 使用策略模式处理不同类型的事件 (处理文章索引利于搜索引擎收录)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleEventHandler implements EventHandler<ArticleEventWrapper> {
    
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