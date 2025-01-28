package cn.xu.domain.notification.model.template.impl;

import cn.xu.domain.notification.model.template.AbstractNotificationTemplate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;

public class SystemNotificationTemplate extends AbstractNotificationTemplate {
    
    private final String title;
    
    public SystemNotificationTemplate(String title, String content, Long receiverId) {
        this.title = title;
        this.content = content;
        this.receiverId = receiverId;
    }
    
    @Override
    protected void prepareNotificationData() {
        addExtraInfo("title", title);
    }
    
    @Override
    public SenderType getSenderType() {
        return SenderType.SYSTEM;
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
        return null; // 系统通知没有业务类型
    }
    
    @Override
    public Long getBusinessId() {
        return null; // 系统通知没有业务ID
    }
    
    @Override
    public void validate() {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("通知内容不能为空");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("通知标题不能为空");
        }
        if (receiverId == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }
    }
} 