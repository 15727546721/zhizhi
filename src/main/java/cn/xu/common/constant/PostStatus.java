package cn.xu.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帖子状态值对象
 */
@Getter
@AllArgsConstructor
public enum PostStatus {
    /**
     * 帖子状态枚举
     */
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    DELETED(2, "已删除"),
    ARCHIVED(3, "已归档");

    private final int code;
    private final String desc;

    /**
     * 根据编码获取帖子状态
     */
    public static PostStatus fromCode(int code) {
        for (PostStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的帖子状态编码: " + code);
    }

    /**
     * 根据字符串状态获取帖子状态
     */
    public static PostStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return DRAFT;
        }
        switch (status.toUpperCase()) {
            case "DRAFT": return DRAFT;
            case "PUBLISHED": return PUBLISHED;
            case "DELETED": return DELETED;
            case "ARCHIVED": return ARCHIVED;
            default: return DRAFT;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case DRAFT: return "DRAFT";
            case PUBLISHED: return "PUBLISHED";
            case DELETED: return "DELETED";
            case ARCHIVED: return "ARCHIVED";
            default: return "DRAFT";
        }
    }
}