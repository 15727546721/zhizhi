package cn.xu.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会话关系类型枚举
 */
@Getter
@AllArgsConstructor
public enum ConversationRelationType {
    
    ONE_WAY_FOLLOW(0, "单向关注", ""),
    MUTUAL_FOLLOW(1, "互相关注", "互相关注"),
    NORMAL_CHAT(2, "普通聊天", "普通聊天");

    private final Integer code;
    private final String name;
    private final String description;

    public static ConversationRelationType fromCode(Integer code) {
        if (code == null) {
            return ONE_WAY_FOLLOW;
        }
        for (ConversationRelationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return ONE_WAY_FOLLOW;
    }
}
