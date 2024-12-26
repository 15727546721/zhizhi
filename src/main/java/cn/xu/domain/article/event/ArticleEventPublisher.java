package cn.xu.domain.article.event;

import cn.dev33.satoken.stp.StpUtil;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文章事件发布器（生产者）
 */
@Service
public class ArticleEventPublisher {

    private final Disruptor<ArticleEvent> disruptor;

    @Autowired
    public ArticleEventPublisher(Disruptor<ArticleEvent> disruptor) {
        this.disruptor = disruptor;
    }

    public void publishEvent(Long articleId, ArticleEvent.EventType type, boolean isAdd) {
        // 创建事件
        ArticleEvent articleEvent = new ArticleEvent();
        articleEvent.setArticleId(articleId);
        articleEvent.setUserId(StpUtil.getLoginIdAsLong());
        articleEvent.setType(type);
        articleEvent.setAdd(isAdd);

        // 发布事件
        disruptor.publishEvent((event, sequence) -> {
            event.setArticleId(articleEvent.getArticleId());
            event.setType(articleEvent.getType());
            event.setAdd(articleEvent.isAdd());
        });
    }

}

