package cn.xu.domain.article.model.valobj;

import lombok.Getter;

@Getter
public enum ArticleStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    ;
    private final Integer value;
    private final String description;

    ArticleStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
} 