package cn.xu.domain.article.event;


import cn.xu.domain.article.event.strategy.ArticleEventStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleEventListener {

    private final cn.xu.article.domain.event.ArticleEventStrategyFactory factory;

    @EventListener
    public void onEvent(ArticleEvent event) {
        log.info("收到文章事件: {}", event);

        try {
            ArticleEventStrategy strategy = factory.getStrategy(event.getType());
            strategy.handle(event);
        } catch (Exception e) {
            log.error("处理文章事件失败: {}", event, e);
        }
    }
}
