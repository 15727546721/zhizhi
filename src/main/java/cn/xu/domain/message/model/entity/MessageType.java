package cn.xu.domain.message.model.entity;

public enum MessageType {
    SYSTEM(1, "系统消息"),
    PRIVATE(2, "私信消息"),
    LIKE(3, "点赞消息"),
    FAVORITE(4, "收藏消息"),
    COMMENT(5, "评论消息"),
    FOLLOW(6, "关注消息");

    private final Integer code;
    private final String desc;

    MessageType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MessageType fromCode(Integer code) {
        for (MessageType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid message type code: " + code);
    }
} 