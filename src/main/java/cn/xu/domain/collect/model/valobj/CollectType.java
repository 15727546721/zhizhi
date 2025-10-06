package cn.xu.domain.collect.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 收藏类型枚举
 */
@Getter
@AllArgsConstructor
public enum CollectType {
    POST("post", "帖子"),
    ESSAY("essay", "文章"),
    COMMENT("comment", "评论");

    private final String code;
    private final String desc;

    public static CollectType fromCode(String code) {
        for (CollectType type : values()) {
            if (type.code.equals(code.toLowerCase())) {
                return type;
            }
        }
        // 默认返回POST类型
        return POST;
    }
}