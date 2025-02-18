package cn.xu.domain.notification.model.template.impl;

import cn.xu.domain.notification.model.template.AbstractNotificationTemplate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.Getter;

/**
 * 收藏通知模板
 */
@Getter
public class FavoriteNotificationTemplate extends AbstractNotificationTemplate {

    private final Long senderId;
    private final Long receiverId;
    private final BusinessType notificationBusinessType;
    private final Long businessId;
    private final String senderName;

    public FavoriteNotificationTemplate(Long senderId, Long receiverId, BusinessType notificationBusinessType, Long businessId, String senderName) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.notificationBusinessType = notificationBusinessType;
        this.businessId = businessId;
        this.senderName = senderName;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.FAVORITE;
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
    public Long getSenderId() {
        return senderId;
    }

    @Override
    public Long getReceiverId() {
        return receiverId;
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
} 