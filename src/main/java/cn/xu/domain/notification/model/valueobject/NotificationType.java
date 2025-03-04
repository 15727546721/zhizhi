package cn.xu.domain.notification.model.valueobject;

import cn.xu.infrastructure.common.exception.BusinessException;
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
    SYSTEM(0, "系统通知"),

    /**
     * 点赞通知
     */
    LIKE(1, "点赞通知"),

    /**
     * 评论通知
     */
    COMMENT(2, "评论通知"),

    /**
     * 回复通知
     */
    REPLY(3, "回复通知"),

    /**
     * 收藏通知
     */
    FAVORITE(4, "收藏通知"),

    /**
     * 关注通知
     */
    FOLLOW(5, "关注通知");

    private final Integer value;
    private final String description;


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
                throw new BusinessException("未知的通知类型: " + this);
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
        throw new BusinessException("无效的通知类型值: " + value);
    }
} 