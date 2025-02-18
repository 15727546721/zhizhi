package cn.xu.domain.notification.model.template.impl;

import cn.xu.domain.notification.model.template.AbstractNotificationTemplate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.infrastructure.common.exception.BusinessException;

public class SystemNotificationTemplate extends AbstractNotificationTemplate {

    private final String title;

    public SystemNotificationTemplate(String title, String content, Long receiverId) {
        this.title = title;
        this.content = content;
        this.receiverId = receiverId;
    }

    @Override
    public Long getSenderId() {
        return null;
    }

    @Override
    public Long getReceiverId() {
        return receiverId;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.SYSTEM;
    }

    @Override
    public BusinessType getBusinessType() {
        return BusinessType.SYSTEM;
    }

    @Override
    public Long getBusinessId() {
        return null; // 系统通知没有业务ID
    }

    @Override
    public void validate() {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("通知内容不能为空");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("通知标题不能为空");
        }
        if (receiverId == null) {
            throw new BusinessException("接收者ID不能为空");
        }
    }
} 