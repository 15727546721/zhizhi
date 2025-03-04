package cn.xu.domain.article.event.factory;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.article.event.DefaultArticleEvent;
import com.lmax.disruptor.EventFactory;
import org.springframework.stereotype.Component;

@Component
public class ArticleEventFactory implements EventFactory<ArticleEvent> {
    @Override
    public ArticleEvent newInstance() {
        return new DefaultArticleEvent();
    }
}
