package cn.xu.domain.like.model;

import lombok.Getter;

/**
 * 点赞类型
 */
@Getter
public enum LikeType {

    ARTICLE(1, "文章"),
    COMMENT(2, "评论"),
    ESSAY(3, "随笔"),
    ;

    private final int code;
    private final String description;

    LikeType(int value, String description) {
        this.code = value;
        this.description = description;
    }

    public static LikeType valueOf(int code) {
        for (LikeType likeType : LikeType.values()) {
            if (likeType.getCode() == code) {
                return likeType;
            }
        }
        return null;
    }
} 