package cn.xu.domain.article.model.aggregate;

import lombok.Data;

@Data
public class ArticleAndTagAgg {
    private Long articleId;
    private String tags;
}
