package cn.xu.domain.article.model.aggregate;

import lombok.Data;

import java.util.List;

@Data
public class ArticleAndTagAgg {
    private Long articleId;
    private String tags;
}
