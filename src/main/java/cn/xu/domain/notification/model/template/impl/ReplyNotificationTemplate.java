package cn.xu.domain.notification.model.template.impl;

import cn.xu.domain.notification.model.template.AbstractNotificationTemplate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;

public class ReplyNotificationTemplate extends AbstractNotificationTemplate {
    
    private final Long businessId;
    private final String replyContent;
    private final BusinessType businessType;
    
    public ReplyNotificationTemplate(Long senderId, Long receiverId, BusinessType businessType, 
            Long businessId, String senderName, String replyContent) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.businessType = businessType;
        this.businessId = businessId;
        this.senderName = senderName;
        this.replyContent = replyContent;
    }
    
    @Override
    protected void prepareNotificationData() {
        super.prepareNotificationData();
        this.content = senderName + "回复了你的" + businessType.getDescription() + "：" + replyContent;
        addExtraInfo("replyContent", replyContent);
    }
    
    @Override
    public NotificationType getType() {
        return NotificationType.REPLY;
    }
    
    @Override
    public BusinessType getBusinessType() {
        return businessType;
    }
    
    @Override
    public Long getBusinessId() {
        return businessId;
    }

    @Override
    public SenderType getSenderType() {
        return SenderType.USER;
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
            throw new IllegalArgumentException("发送者ID不能为空");
        }
        if (receiverId == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }
        if (businessId == null) {
            throw new IllegalArgumentException("业务ID不能为空");
        }
        if (businessType == null) {
            throw new IllegalArgumentException("业务类型不能为空");
        }
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new IllegalArgumentException("回复内容不能为空");
        }
        if (senderName == null || senderName.trim().isEmpty()) {
            throw new IllegalArgumentException("发送者名称不能为空");
        }
    }
} 