package cn.xu.domain.article.event;

import cn.xu.domain.article.model.entity.ArticleEntity;
import com.lmax.disruptor.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 文章领域事件发布者
 * 负责文章领域事件的发布
 */
@Slf4j
@Service
public class ArticleEventPublisher {

    private final RingBuffer<ArticleEventWrapper> eventRingBuffer;

    public ArticleEventPublisher(RingBuffer<ArticleEventWrapper> eventRingBuffer) {
        this.eventRingBuffer = eventRingBuffer;
    }

    /**
     * 发布领域事件
     *
     * @param event 领域事件
     */
    private void publishEvent(ArticleEvent event) {
        long sequence = eventRingBuffer.next();
        try {
            ArticleEventWrapper eventWrapper = eventRingBuffer.get(sequence);
            eventWrapper.setEvent(event);
            log.info("发布文章领域事件：{}", event);
        } finally {
            eventRingBuffer.publish(sequence);
        }
    }

    /**
     * 发布文章创建事件
     */
    public void publishArticleCreated(ArticleEntity article) {
        ArticleCreatedEvent event = new ArticleCreatedEvent(
                article.getId(),
                article.getTitle(),
                article.getDescription(),
                article.getContent(),
                article.getUserId()
        );
        publishEvent(event);
    }

    /**
     * 发布文章更新事件
     */
    public void publishArticleUpdated(ArticleEntity article) {
        ArticleUpdatedEvent event = new ArticleUpdatedEvent(
                article.getId(),
                article.getTitle(),
                article.getDescription(),
                article.getContent(),
                article.getUserId()
        );
        publishEvent(event);
    }

    /**
     * 发布文章删除事件
     */
    public void publishArticleDeleted(Long articleId) {
        publishEvent(new ArticleDeletedEvent(articleId));
    }
} 