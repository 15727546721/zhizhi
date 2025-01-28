package cn.xu.domain.notification.model.template;

import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;
import lombok.Getter;

/**
 * 点赞通知模板
 *
 * @author xuhh
 * @date 2024/03/20
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
    private final BusinessType businessType;

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
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param senderName 发送者名称
     */
    public LikeNotificationTemplate(Long senderId, Long receiverId, BusinessType businessType, Long businessId, String senderName) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.businessType = businessType;
        this.businessId = businessId;
        this.senderName = senderName;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.LIKE;
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
    protected void prepareNotificationData() {
        super.prepareNotificationData();
        this.content = String.format("%s赞了你的%s", senderName, businessType.getDescription());
    }

    @Override
    public void validate() {
        if (senderId == null) {
            throw new IllegalArgumentException("发送者ID不能为空");
        }
        if (receiverId == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }
        if (businessType == null) {
            throw new IllegalArgumentException("业务类型不能为空");
        }
        if (businessId == null) {
            throw new IllegalArgumentException("业务ID不能为空");
        }
        if (senderName == null || senderName.trim().isEmpty()) {
            throw new IllegalArgumentException("发送者名称不能为空");
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