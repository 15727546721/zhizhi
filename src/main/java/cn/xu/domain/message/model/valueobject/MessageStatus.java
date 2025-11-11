package cn.xu.domain.message.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息状态值对象
 */
@Getter
@AllArgsConstructor
public enum MessageStatus {
    /**
     * 正常送达（对方可收到）
     */
    DELIVERED(1, "正常送达"),
    
    /**
     * 未送达（对方收不到，仅数据库记录）
     */
    PENDING(2, "未送达"),
    
    /**
     * 被屏蔽（接收者屏蔽了发送者，消息仅发送者可见）
     */
    BLOCKED(3, "被屏蔽");
    
    private final int code;
    private final String description;
    
    /**
     * 根据代码获取消息状态
     */
    public static MessageStatus fromCode(int code) {
        for (MessageStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid message status code: " + code);
    }
    
    /**
     * 判断是否为正常送达
     */
    public boolean isDelivered() {
        return this == DELIVERED;
    }
    
    /**
     * 判断是否为未送达
     */
    public boolean isPending() {
        return this == PENDING;
    }
    
    /**
     * 判断是否为被屏蔽
     */
    public boolean isBlocked() {
        return this == BLOCKED;
    }
}

