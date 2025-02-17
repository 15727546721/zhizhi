package cn.xu.domain.notification.model.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 通知类型值对象
 * 定义所有可能的通知类型及其与业务类型的关联
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {
    /**
     * 系统通知
     */
    SYSTEM("系统通知"),
    
    /**
     * 点赞通知
     */
    LIKE("点赞通知"),
    
    /**
     * 评论通知
     */
    COMMENT("评论通知"),
    
    /**
     * 回复通知
     */
    REPLY("回复通知"),
    
    /**
     * 收藏通知
     */
    FAVORITE("收藏通知"),
    
    /**
     * 关注通知
     */
    FOLLOW("关注通知");

    private final String description;



    /**
     * 检查是否需要业务ID
     */
    public boolean requiresBusinessId() {
        return this != SYSTEM && this != FOLLOW;
    }

    /**
     * 检查是否是用户触发的通知
     */
    public boolean isUserTriggered() {
        return this != SYSTEM;
    }

    /**
     * 获取通知的优先级（可用于排序）
     */
    public int getPriority() {
        switch (this) {
            case SYSTEM:
                return 0;  // 系统通知最高优先级
            case COMMENT:
            case REPLY:
                return 1;  // 评论和回复次之
            case LIKE:
            case FAVORITE:
                return 2;  // 点赞和收藏再次之
            case FOLLOW:
                return 3;  // 关注最低优先级
            default:
                throw new IllegalStateException("未知的通知类型: " + this);
        }
    }

    /**
     * 获取通知类型的值（用于数据库存储）
     */
    public int getValue() {
        return this.ordinal();
    }

    /**
     * 根据值获取通知类型
     */
    public static NotificationType fromValue(int value) {
        for (NotificationType type : values()) {
            if (type.ordinal() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的通知类型值: " + value);
    }
} 