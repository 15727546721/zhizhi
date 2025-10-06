package cn.xu.domain.like.model;

import lombok.Getter;

/**
 * 点赞类型
 */
@Getter
public enum LikeType {

    POST(1, "帖子"),
    ESSAY(2, "随笔"),
    COMMENT(3, "评论"),
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
    
    /**
     * 根据名称获取LikeType
     * @param name 枚举名称
     * @return LikeType枚举
     */
    public static LikeType fromName(String name) {
        try {
            return LikeType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * 获取用于Redis键的名称（小写）
     * @return Redis键名
     */
    public String getRedisKeyName() {
        return this.name().toLowerCase();
    }
}