package cn.xu.common.constants;

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

    /**
     * 类型码
     */
    private final Integer code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 显示描述
     */
    private final String description;

    /**
     * 根据类型码获取枚举
     *
     * @param code 类型码
     * @return 对应的枚举，未找到返回 ONE_WAY_FOLLOW
     */
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
