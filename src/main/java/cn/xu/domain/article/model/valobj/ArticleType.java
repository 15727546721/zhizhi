package cn.xu.domain.article.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleType {
    RECOMMEND("recommend", "推荐"),
    NEW("new", "最新");

    private final String value;
    private final String message;
} 