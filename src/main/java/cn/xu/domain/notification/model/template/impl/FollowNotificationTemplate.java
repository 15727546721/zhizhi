package cn.xu.domain.notification.model.template.impl;

import cn.xu.domain.notification.model.template.AbstractNotificationTemplate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.Getter;

/**
 * 关注通知模板
 */
@Getter
public class FollowNotificationTemplate extends AbstractNotificationTemplate {

    private final Long senderId;
    private final Long receiverId;
    private final String senderName;

    public FollowNotificationTemplate(Long senderId, Long receiverId, String senderName) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderName = senderName;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.FOLLOW;
    }

    @Override
    public BusinessType getBusinessType() {
        return BusinessType.USER;
    }

    @Override
    public Long getBusinessId() {
        return senderId;
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
        if (senderName == null || senderName.trim().isEmpty()) {
            throw new BusinessException("发送者名称不能为空");
        }
    }
} 