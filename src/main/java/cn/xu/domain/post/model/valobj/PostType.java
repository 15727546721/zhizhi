package cn.xu.domain.post.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帖子类型值对象
 */
@Getter
@AllArgsConstructor
public enum PostType {
    
    /**
     * 帖子类型枚举
     */
    POST("POST", "帖子"),
    DISCUSSION("DISCUSSION", "讨论"),
    QUESTION("QUESTION", "问答"),
    ARTICLE("ARTICLE", "文章"),
    RESOURCE("RESOURCE", "资源分享"),
    ANSWER("ANSWER", "回答"); // 新增回答类型，专门用于问答帖的回答
    
    private final String code;
    private final String desc;
    
    /**
     * 根据编码获取帖子类型
     */
    public static PostType fromCode(String code) {
        for (PostType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        // 默认返回POST类型，避免IllegalArgumentException
        return POST;
    }
    
    @Override
    public String toString() {
        return code;
    }
}