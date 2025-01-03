package cn.xu.domain.like.model;

import lombok.Getter;

/**
 * 点赞类型枚举
 */
@Getter
public enum LikeType {
    ARTICLE(1, "文章"),
    TOPIC(2, "话题"),
    COMMENT(3, "评论");

    private final int code;
    private final String description;

    LikeType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static LikeType fromCode(int code) {
        for (LikeType type : LikeType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的点赞类型编码: " + code);
    }

    public static LikeType fromName(String name) {
        try {
            return LikeType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的点赞类型名称: " + name);
        }
    }
} 