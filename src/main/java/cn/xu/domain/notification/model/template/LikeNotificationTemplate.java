package cn.xu.domain.notification.model.template;

import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.Getter;

/**
 * 点赞通知模板
 */
@Getter
public class LikeNotificationTemplate extends AbstractNotificationTemplate {

    /**
     * 发送者ID
     */
    private final Long senderId;

    /**
     * 接收者ID
     */
    private final Long receiverId;

    /**
     * 业务类型
     */
    private final BusinessType notificationBusinessType;

    /**
     * 业务ID
     */
    private final Long businessId;

    /**
     * 发送者名称
     */
    private final String senderName;

    /**
     * 构造函数
     *
     * @param senderId                 发送者ID
     * @param receiverId               接收者ID
     * @param notificationBusinessType 业务类型
     * @param businessId               业务ID
     * @param senderName               发送者名称
     */
    public LikeNotificationTemplate(Long senderId, Long receiverId, BusinessType notificationBusinessType, Long businessId, String senderName) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.notificationBusinessType = notificationBusinessType;
        this.businessId = businessId;
        this.senderName = senderName;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.LIKE;
    }

    @Override
    public BusinessType getBusinessType() {
        return notificationBusinessType;

    }

    @Override
    public Long getBusinessId() {
        return businessId;
    }

    @Override
    public void validate() {
        if (senderId == null) {
            throw new BusinessException("发送者ID不能为空");
        }
        if (receiverId == null) {
            throw new BusinessException("接收者ID不能为空");
        }
        if (notificationBusinessType == null) {
            throw new BusinessException("业务类型不能为空");
        }
        if (businessId == null) {
            throw new BusinessException("业务ID不能为空");
        }
        if (senderName == null || senderName.trim().isEmpty()) {
            throw new BusinessException("发送者名称不能为空");
        }
    }

    @Override
    public Long getSenderId() {
        return senderId;
    }

    @Override
    public Long getReceiverId() {
        return receiverId;
    }
} 