package cn.xu.domain.like.model;

import lombok.Getter;

/**
 * 点赞类型
 */
@Getter
public enum LikeType {

    ARTICLE(1, "文章"),
    COMMENT(2, "评论"),
    TOPIC(3, "话题"),
    ;

    private final int value;
    private final String description;

    LikeType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static LikeType valueOf(int value) {
        for (LikeType likeType : LikeType.values()) {
            if (likeType.getValue() == value) {
                return likeType;
            }
        }
        return null;
    }
} 