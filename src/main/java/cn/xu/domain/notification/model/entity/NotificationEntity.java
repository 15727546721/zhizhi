package cn.xu.domain.notification.model.entity;

import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者类型
     */
    private SenderType senderType;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 通知类型
     */
    private NotificationType type;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 业务类型
     */
    private BusinessType businessType;
    
    /**
     * 业务ID
     */
    private Long businessId;
    
    /**
     * 额外信息
     */
    private Map<String, Object> extraInfo;
    
    /**
     * 是否已读
     */
    private boolean read;
    
    /**
     * 已读时间
     */
    private LocalDateTime readTime;
    
    /**
     * 是否有效
     */
    private Boolean status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 检查是否已读
     */
    public boolean isRead() {
        return Boolean.TRUE.equals(read);
    }

    /**
     * 验证实体的有效性
     */
    public void validate() {
        if (receiverId == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }
        if (type == null) {
            throw new IllegalArgumentException("通知类型不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("通知内容不能为空");
        }
        if (senderType == null) {
            throw new IllegalArgumentException("发送者类型不能为空");
        }
        if (SenderType.USER.equals(senderType) && senderId == null) {
            throw new IllegalArgumentException("用户类型的发送者ID不能为空");
        }
        if (businessType != null && businessId == null) {
            throw new IllegalArgumentException("业务类型存在时，业务ID不能为空");
        }
        if (type == NotificationType.SYSTEM && (title == null || title.trim().isEmpty())) {
            throw new IllegalArgumentException("系统通知的标题不能为空");
        }
    }

    public void setRead(boolean read) {
        this.read = read;
        if (read && this.readTime == null) {
            this.readTime = LocalDateTime.now();
        }
        this.updatedTime = LocalDateTime.now();
    }
} 