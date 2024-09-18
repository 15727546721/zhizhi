package cn.xu.domain.article.model.valobj;

public enum ArticleStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "发布"),
    UNPUBLISHED(2, "下架"),
    PENDING_REVIEW(3, "待审核"),
    REVIEW_FAILED(4, "审核不通过");

    private final int code;
    private final String description;

    // 构造函数
    ArticleStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // 获取状态代码
    public int getCode() {
        return code;
    }

    // 获取状态描述
    public String getDescription() {
        return description;
    }

    // 根据状态代码获取枚举实例
    public static ArticleStatus fromCode(int code) {
        for (ArticleStatus status : ArticleStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}

