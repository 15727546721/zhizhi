package cn.xu.domain.article.event.strategy;


import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.infrastructure.persistent.read.elastic.service.ArticleElasticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractArticleStrategy implements ArticleEventStrategy {

    @Autowired(required = false) // 设置为非必需，允许Elasticsearch不可用
    protected ArticleElasticService elasticService;

    protected ArticleEntity toEntity(ArticleEvent event) {
        return ArticleEntity.builder()
                .id(event.getArticleId())
                .title(new cn.xu.domain.article.model.valobj.ArticleTitle(event.getTitle()))
                .description(event.getDescription())
                .content(new cn.xu.domain.article.model.valobj.ArticleContent(event.getContent()))
                .userId(event.getUserId())
                .build();
    }
    
    // 添加检查Elasticsearch是否可用的方法
    protected boolean isElasticsearchAvailable() {
        if (elasticService == null) {
            log.debug("Elasticsearch服务不可用");
            return false;
        }
        return true;
    }
}