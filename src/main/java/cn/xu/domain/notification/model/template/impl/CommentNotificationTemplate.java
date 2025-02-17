package cn.xu.domain.notification.model.template.impl;

import cn.xu.domain.notification.model.template.AbstractNotificationTemplate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;
import lombok.Getter;

/**
 * 评论通知模板
 *
 */
@Getter
public class CommentNotificationTemplate extends AbstractNotificationTemplate {

    private final Long senderId;
    private final Long receiverId;
    private final BusinessType notificationBusinessType;
    private final Long businessId;
    private final String senderName;
    private final String commentContent;

    public CommentNotificationTemplate(Long senderId, Long receiverId, BusinessType notificationBusinessType, Long businessId,
                                       String senderName, String commentContent) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.notificationBusinessType = notificationBusinessType;
        this.businessId = businessId;
        this.senderName = senderName;
        this.commentContent = commentContent;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.COMMENT;
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
    protected void prepareNotificationData() {
        super.prepareNotificationData();
        this.content = String.format("%s评论了你的%s：%s", senderName, BusinessType.ARTICLE.getDescription(), commentContent);
    }

    @Override
    public void validate() {
        if (senderId == null) {
            throw new IllegalArgumentException("发送者ID不能为空");
        }
        if (receiverId == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }
        if (notificationBusinessType == null) {
            throw new IllegalArgumentException("业务类型不能为空");
        }
        if (businessId == null) {
            throw new IllegalArgumentException("业务ID不能为空");
        }
        if (senderName == null || senderName.trim().isEmpty()) {
            throw new IllegalArgumentException("发送者名称不能为空");
        }
        if (commentContent == null || commentContent.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
    }
} 