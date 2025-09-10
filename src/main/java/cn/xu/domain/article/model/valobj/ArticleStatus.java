package cn.xu.domain.article.model.valobj;

import lombok.Getter;

@Getter
public enum ArticleStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    DELETED(2, "已删除"),
    ;
    private final Integer value;
    private final String description;

    ArticleStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static ArticleStatus valueOf(Integer value) {
        for (ArticleStatus status : ArticleStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据状态码获取文章状态
     *
     * @param code 状态码
     * @return 文章状态
     */
    public static ArticleStatus fromCode(Integer code) {
        if (code == null) {
            return DRAFT;
        }
        for (ArticleStatus status : ArticleStatus.values()) {
            if (status.getValue().equals(code)) {
                return status;
            }
        }
        return DRAFT;
    }

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    public Integer getCode() {
        return value;
    }
} 