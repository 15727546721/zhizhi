package cn.xu.domain.like.model;

import lombok.Getter;

/**
 * 点赞状态枚举
 */
@Getter
public enum LikeStatus {
    UNLIKED(0, "取消点赞"),
    LIKED(1, "已点赞");

    private final int code;
    private final String description;

    LikeStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static LikeStatus valueOf(int code) {
        for (LikeStatus status : LikeStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}