package cn.xu.domain.article.event;

import com.lmax.disruptor.EventFactory;
import org.springframework.stereotype.Component;

@Component
public class ArticleEventFactory implements EventFactory<ArticleEvent> {
    @Override
    public ArticleEvent newInstance() {
        return new ArticleEvent();
    }
}

