package cn.xu.domain.notification.event;

import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知事件基类
 * 用于在领域内传递通知相关的事件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    /**
     * 通知类型
     */
    private NotificationType type;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

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
     * 创建时间
     */
    private LocalDateTime createTime;

} 