package cn.xu.domain.notification.model.aggregate;

import cn.xu.domain.notification.model.entity.NotificationEntity;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationSender;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 通知聚合根
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class NotificationAggregate {

    private NotificationEntity notification;
    @Getter
    private NotificationSender sender;

    private NotificationAggregate(NotificationEntity notification) {
        this.notification = notification;
    }

    public static NotificationAggregate from(NotificationEntity entity) {
        return new NotificationAggregate(entity);
    }

    public static class NotificationAggregateBuilder {
        private Long id;
        private NotificationType type;
        private Long senderId;
        private Long receiverId;
        private String title;
        private String content;
        private BusinessType businessType;
        private Long businessId;
        private Boolean read;
        private Boolean status;
        private LocalDateTime createdTime;

        NotificationAggregateBuilder() {
        }

        public NotificationAggregateBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public NotificationAggregateBuilder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public NotificationAggregateBuilder senderId(Long senderId) {
            this.senderId = senderId;
            return this;
        }

        public NotificationAggregateBuilder receiverId(Long receiverId) {
            this.receiverId = receiverId;
            return this;
        }

        public NotificationAggregateBuilder title(String title) {
            this.title = title;
            return this;
        }

        public NotificationAggregateBuilder content(String content) {
            this.content = content;
            return this;
        }

        public NotificationAggregateBuilder businessType(BusinessType notificationBusinessType) {
            this.businessType = notificationBusinessType;
            return this;
        }

        public NotificationAggregateBuilder businessId(Long businessId) {
            this.businessId = businessId;
            return this;
        }

        public NotificationAggregateBuilder read(Boolean read) {
            this.read = read;
            return this;
        }

        public NotificationAggregateBuilder status(Boolean status) {
            this.status = status;
            return this;
        }

        public NotificationAggregateBuilder createdTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public NotificationAggregate build() {
            return new NotificationAggregate(NotificationEntity.builder()
                    .id(this.id)
                    .type(this.type)
                    .senderId(this.senderId)
                    .receiverId(this.receiverId)
                    .title(this.title)
                    .content(this.content)
                    .businessType(this.businessType)
                    .businessId(this.businessId)
                    .read(this.read != null ? this.read : false)
                    .status(this.status != null ? this.status : true)
                    .createTime(this.createdTime != null ? this.createdTime : LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build());
        }
    }

    public Long getId() {
        return notification.getId();
    }

    public void markAsRead() {
        notification.setRead(true);
        notification.setUpdateTime(LocalDateTime.now());
    }

    public boolean isRead() {
        return notification.isRead();
    }

    /**
     * 获取接收者ID
     */
    public Long getReceiverId() {
        return notification.getReceiverId();
    }

    /**
     * 获取发送者ID
     */
    public Long getSenderId() {
        return notification.getSenderId();
    }

    public NotificationType getType() {
        return notification.getType();
    }

    public BusinessType getBusinessType() {
        return notification.getBusinessType();
    }

    public Long getBusinessId() {
        return notification.getBusinessId();
    }

    public String getTitle() {
        return notification.getTitle();
    }

    public String getContent() {
        return notification.getContent();
    }

    public LocalDateTime getCreateTime() {
        return notification.getCreateTime();
    }

    // 领域行为：标记为删除
    public void markAsDeleted() {
        if (Boolean.FALSE.equals(this.notification.getStatus())) {
            return;
        }
        this.notification.setStatus(false);
        this.notification.setUpdateTime(LocalDateTime.now());
    }

    // 领域行为：更新通知内容
    public void updateContent(String newContent) {
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("通知内容不能为空");
        }
        this.notification.setContent(newContent.trim());
        this.notification.setUpdateTime(LocalDateTime.now());
    }

    public void setContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("通知内容不能为空");
        }
        this.notification.setContent(content.trim());
        this.notification.setUpdateTime(LocalDateTime.now());
    }

    // 领域行为：检查是否属于指定用户
    public boolean belongsToUser(Long userId) {
        return this.notification.getReceiverId() != null && this.notification.getReceiverId().equals(userId);
    }

    // 领域行为：检查是否已过期（例如7天后自动过期）
    public boolean isExpired() {
        if (this.notification.getCreateTime() == null) {
            return false;
        }
        return this.notification.getCreateTime().plusDays(7).isBefore(LocalDateTime.now());
    }

    // 不变量检查
    public void validate() {
        notification.validate();
    }

    // 工厂方法：创建系统通知
    public static NotificationAggregate createSystemNotification(String title, String content, Long userId) {
        return builder()
                .type(NotificationType.SYSTEM)
                .receiverId(userId)
                .title(title)
                .content(content)
                .read(false)
                .status(true)
                .build();
    }

    // 工厂方法：创建点赞通知
    public static NotificationAggregate createLikeNotification(Long senderId, Long userId, Long businessId, BusinessType notificationBusinessType) {
        return builder()
                .type(NotificationType.LIKE)
                .receiverId(userId)
                .senderId(senderId)
                .content("赞了你的" + notificationBusinessType.getDescription())
                .businessType(notificationBusinessType)
                .businessId(businessId)
                .read(false)
                .status(true)
                .build();
    }

    // 工厂方法：创建评论通知
    public static NotificationAggregate createCommentNotification(Long senderId, Long userId, String content, Long articleId, BusinessType notificationBusinessType) {
        return builder()
                .type(NotificationType.COMMENT)
                .receiverId(userId)
                .senderId(senderId)
                .content(content)
                .businessType(notificationBusinessType)
                .businessId(articleId)
                .read(false)
                .status(true)
                .build();
    }

    // 工厂方法：创建关注通知
    public static NotificationAggregate createFollowNotification(Long senderId, Long userId, BusinessType notificationBusinessType) {
        return builder()
                .type(NotificationType.FOLLOW)
                .receiverId(userId)
                .senderId(senderId)
                .content("关注了你")
                .businessType(notificationBusinessType.USER)
                .businessId(userId)
                .read(false)
                .status(true)
                .build();
    }

    public void updateSender(NotificationSender sender) {
        this.sender = sender;
        this.notification.setSenderId(sender.getSenderId());
        // 移除了与 extraInfo 相关的字段
        this.notification.setUpdateTime(LocalDateTime.now());
    }
}
