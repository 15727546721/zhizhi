package cn.xu.domain.article.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleType {
    RECOMMEND(1, "推荐"),
    NEW(2, "最新");

    private final Integer code;
    private final String message;
} 