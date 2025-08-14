package cn.xu.domain.article.event.strategy;


import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.infrastructure.persistent.read.elastic.service.ArticleElasticService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor
public abstract class AbstractArticleStrategy implements ArticleEventStrategy {

    @Autowired
    protected ArticleElasticService elasticService;

    protected ArticleEntity toEntity(ArticleEvent event) {
        return ArticleEntity.builder()
                .id(event.getArticleId())
                .title(event.getTitle())
                .description(event.getDescription())
                .content(event.getContent())
                .userId(event.getUserId())
                .build();
    }
}

