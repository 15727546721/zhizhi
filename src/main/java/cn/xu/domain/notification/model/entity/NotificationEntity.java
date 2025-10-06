package cn.xu.domain.notification.model.entity;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
     * 是否已读
     */
    private boolean read;

    /**
     * 已读时间
     */
    private LocalDateTime readTime;

    /**
     * 状态：1-有效 2-已删除
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 检查是否已读
     */
    public boolean isRead() {
        return Boolean.TRUE.equals(read);
    }

    /**
     * 检查状态是否有效
     */
    public boolean isStatus() {
        return Boolean.TRUE.equals(status);
    }

    /**
     * 验证实体的有效性
     */
    public void validate() {
        if (receiverId == null) {
            throw new BusinessException("接收者ID不能为空");
        }
        if (type == null) {
            throw new BusinessException("通知类型不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("通知内容不能为空");
        }
        if (businessType != null && businessId == null) {
            throw new BusinessException("业务类型存在时，业务ID不能为空");
        }
        if (type == NotificationType.SYSTEM && (title == null || title.trim().isEmpty())) {
            throw new BusinessException("系统通知的标题不能为空");
        }
    }

    public void setRead(boolean read) {
        this.read = read;
        if (read && this.readTime == null) {
            this.readTime = LocalDateTime.now();
        }
        this.updateTime = LocalDateTime.now();
    }
} 