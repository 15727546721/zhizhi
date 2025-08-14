package cn.xu.domain.article.event;


import cn.xu.domain.article.model.entity.ArticleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ArticleEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishCreated(ArticleEntity article) {
        publish(article, ArticleEvent.ArticleEventType.CREATED);
    }

    public void publishUpdated(ArticleEntity article) {
        publish(article, ArticleEvent.ArticleEventType.UPDATED);
    }

    public void publishDeleted(Long articleId) {
        publisher.publishEvent(ArticleEvent.builder()
                .articleId(articleId)
                .type(ArticleEvent.ArticleEventType.DELETED)
                .build());
    }

    private void publish(ArticleEntity article, ArticleEvent.ArticleEventType type) {
        publisher.publishEvent(ArticleEvent.builder()
                .articleId(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .content(article.getContent())
                .userId(article.getUserId())
                .type(type)
                .build());
    }
}
