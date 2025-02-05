package cn.xu.domain.notification.model.aggregate;

import cn.xu.domain.notification.model.entity.NotificationEntity;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationSender;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知聚合根
 *
 * @author xuhh
 * @date 2024/03/20
 */
@Slf4j
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class NotificationAggregate {
    
    private NotificationEntity notification;
    private NotificationSender sender;

    private NotificationAggregate(NotificationEntity notification) {
        this.notification = notification;
        this.sender = NotificationSender.of(
            notification.getSenderId(),
            notification.getSenderType(),
            (String) notification.getExtraInfo().get("senderName"),
            (String) notification.getExtraInfo().get("senderAvatar")
        );
    }

    public static NotificationAggregate from(NotificationEntity entity) {
        return new NotificationAggregate(entity);
    }
    
    public static NotificationAggregateBuilder builder() {
        return new NotificationAggregateBuilder();
    }
    
    public static class NotificationAggregateBuilder {
        private Long id;
        private NotificationType type;
        private Long senderId;
        private SenderType senderType;
        private Long receiverId;
        private String title;
        private String content;
        private BusinessType businessType;
        private Long businessId;
        private Map<String, Object> extraInfo;
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
        
        public NotificationAggregateBuilder senderType(SenderType senderType) {
            this.senderType = senderType;
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
        
        public NotificationAggregateBuilder businessType(BusinessType businessType) {
            this.businessType = businessType;
            return this;
        }
        
        public NotificationAggregateBuilder businessId(Long businessId) {
            this.businessId = businessId;
            return this;
        }
        
        public NotificationAggregateBuilder extraInfo(Map<String, Object> extraInfo) {
            this.extraInfo = extraInfo;
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
            NotificationEntity entity = new NotificationEntity();
            entity.setId(this.id);
            entity.setType(this.type);
            entity.setSenderId(this.senderId);
            entity.setSenderType(this.senderType);
            entity.setReceiverId(this.receiverId);
            entity.setTitle(this.title);
            entity.setContent(this.content);
            entity.setBusinessType(this.businessType);
            entity.setBusinessId(this.businessId);
            entity.setExtraInfo(this.extraInfo != null ? this.extraInfo : new HashMap<>());
            entity.setRead(this.read != null ? this.read : false);
            entity.setStatus(this.status != null ? this.status : true);
            entity.setCreatedTime(this.createdTime != null ? this.createdTime : LocalDateTime.now());
            entity.setUpdatedTime(LocalDateTime.now());
            return new NotificationAggregate(entity);
        }
    }
    
    public Long getId() {
        return notification.getId();
    }
    
    public void markAsRead() {
        notification.setRead(true);
        notification.setUpdatedTime(LocalDateTime.now());
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
    
    public Map<String, Object> getExtraInfo() {
        return notification.getExtraInfo();
    }
    
    public LocalDateTime getCreatedTime() {
        return notification.getCreatedTime();
    }

    // 领域行为：标记为删除
    public void markAsDeleted() {
        if (Boolean.FALSE.equals(this.notification.getStatus())) {
            return;
        }
        this.notification.setStatus(false);
        this.notification.setUpdatedTime(LocalDateTime.now());
    }

    // 领域行为：更新通知内容
    public void updateContent(String newContent) {
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("通知内容不能为空");
        }
        this.notification.setContent(newContent.trim());
        this.notification.setUpdatedTime(LocalDateTime.now());
    }

    public void setContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("通知内容不能为空");
        }
        this.notification.setContent(content.trim());
        this.notification.setUpdatedTime(LocalDateTime.now());
    }

    // 领域行为：添加额外信息
    public void addExtraInfo(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("额外信息的键不能为空");
        }
        if (this.notification.getExtraInfo() == null) {
            throw new IllegalStateException("额外信息Map未初始化");
        }
        this.notification.getExtraInfo().put(key, value);
        this.notification.setUpdatedTime(LocalDateTime.now());
    }

    // 领域行为：检查是否属于指定用户
    public boolean belongsToUser(Long userId) {
        return this.notification.getReceiverId() != null && this.notification.getReceiverId().equals(userId);
    }

    // 领域行为：检查是否是系统通知
    public boolean isSystemNotification() {
        return NotificationType.SYSTEM.equals(this.notification.getType()) && 
               SenderType.SYSTEM.equals(this.notification.getSenderType());
    }

    // 领域行为：检查是否已过期（例如7天后自动过期）
    public boolean isExpired() {
        if (this.notification.getCreatedTime() == null) {
            return false;
        }
        return this.notification.getCreatedTime().plusDays(7).isBefore(LocalDateTime.now());
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
                .senderType(SenderType.SYSTEM)
                .title(title)
                .content(content)
                .read(false)
                .status(true)
                .build();
    }

    // 工厂方法：创建点赞通知
    public static NotificationAggregate createLikeNotification(Long senderId, Long userId, Long businessId, BusinessType businessType) {
        return builder()
                .type(NotificationType.LIKE)
                .receiverId(userId)
                .senderId(senderId)
                .senderType(SenderType.USER)
                .content("赞了你的" + businessType.getDescription())
                .businessType(businessType)
                .businessId(businessId)
                .read(false)
                .status(true)
                .build();
    }

    // 工厂方法：创建评论通知
    public static NotificationAggregate createCommentNotification(Long senderId, Long userId, String content, Long articleId) {
        return builder()
                .type(NotificationType.COMMENT)
                .receiverId(userId)
                .senderId(senderId)
                .senderType(SenderType.USER)
                .content(content)
                .businessType(BusinessType.ARTICLE)
                .businessId(articleId)
                .read(false)
                .status(true)
                .build();
    }

    // 工厂方法：创建关注通知
    public static NotificationAggregate createFollowNotification(Long senderId, Long userId) {
        return builder()
                .type(NotificationType.FOLLOW)
                .receiverId(userId)
                .senderId(senderId)
                .senderType(SenderType.USER)
                .content("关注了你")
                .businessType(BusinessType.USER)
                .businessId(userId)
                .read(false)
                .status(true)
                .build();
    }

    public NotificationSender getSender() {
        return sender;
    }

    public void updateSender(NotificationSender sender) {
        this.sender = sender;
        this.notification.setSenderId(sender.getSenderId());
        this.notification.setSenderType(sender.getSenderType());
        this.notification.getExtraInfo().put("senderName", sender.getSenderName());
        this.notification.getExtraInfo().put("senderAvatar", sender.getSenderAvatar());
        this.notification.setUpdatedTime(LocalDateTime.now());
    }
} 