package cn.xu.domain.notification.model.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 通知状态值对象
 * 封装通知的状态相关属性，确保状态的不可变性
 */
@Getter
@Builder
public class NotificationStatus {

    private final Boolean isRead;
    private final LocalDateTime readTime;
    private final LocalDateTime createTime;
    private final Boolean isDeleted;
    private final LocalDateTime deleteTime;

    public static NotificationStatus createUnread() {
        return NotificationStatus.builder()
                .isRead(false)
                .readTime(null)
                .createTime(LocalDateTime.now())
                .isDeleted(false)
                .deleteTime(null)
                .build();
    }

    public static NotificationStatus markAsRead() {
        return NotificationStatus.builder()
                .isRead(true)
                .readTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .isDeleted(false)
                .deleteTime(null)
                .build();
    }

    public static NotificationStatus markAsDeleted() {
        return NotificationStatus.builder()
                .isRead(true)
                .readTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .isDeleted(true)
                .deleteTime(LocalDateTime.now())
                .build();
    }

    /**
     * 检查通知是否可读
     *
     * @return 是否可读
     */
    public boolean isReadable() {
        return !isDeleted;
    }

    /**
     * 检查通知是否可删除
     *
     * @return 是否可删除
     */
    public boolean isDeletable() {
        return !isDeleted;
    }
} 