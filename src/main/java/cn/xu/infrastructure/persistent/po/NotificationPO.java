package cn.xu.infrastructure.persistent.po;

import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class NotificationPO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 通知类型
     */
    private NotificationType type;

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
     * 标题
     */
    private String title;

    /**
     * 内容
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
     * 额外信息（JSON格式）
     */
    private Map<String, Object> extraInfo;

    /**
     * 是否已读
     */
    private Boolean read;

    /**
     * 状态（true: 有效, false: 已删除）
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
} 