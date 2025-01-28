package cn.xu.domain.notification.model.template;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.entity.NotificationEntity;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知模板抽象类
 * 使用模板方法模式规范化通知的构建和发送流程
 *
 * @author xuhh
 * @date 2024/03/20
 */
@Getter
public abstract class AbstractNotificationTemplate {
    protected Long senderId;
    protected Long receiverId;
    protected String senderName;
    protected String content;
    protected Map<String, Object> extraInfo = new HashMap<>();

    protected AbstractNotificationTemplate() {
    }

    /**
     * 模板方法：构建并发送通知
     * 定义了通知发送的标准流程
     */
    public final NotificationAggregate build() {
        validate();
        prepareNotificationData();
        return createNotification();
    }

    /**
     * 获取通知类型
     *
     * @return 通知类型
     */
    public abstract NotificationType getType();

    /**
     * 获取业务类型
     *
     * @return 业务类型
     */
    public abstract BusinessType getBusinessType();

    /**
     * 获取业务ID
     *
     * @return 业务ID
     */
    public abstract Long getBusinessId();

    /**
     * 获取发送者类型
     *
     * @return 发送者类型
     */
    public abstract SenderType getSenderType();

    /**
     * 获取发送者ID
     *
     * @return 发送者ID
     */
    public abstract Long getSenderId();

    /**
     * 获取接收者ID
     *
     * @return 接收者ID
     */
    public abstract Long getReceiverId();

    /**
     * 准备通知数据
     */
    protected void prepareNotificationData() {
        // 子类可以覆盖此方法以准备特定的通知数据
    }

    /**
     * 创建通知实体
     *
     * @return 通知聚合根
     */
    protected NotificationAggregate createNotification() {
        NotificationEntity entity = NotificationEntity.builder()
                .type(getType())
                .senderId(getSenderId())
                .senderType(getSenderType())
                .receiverId(getReceiverId())
                .content(content)
                .businessType(getBusinessType())
                .businessId(getBusinessId())
                .extraInfo(new HashMap<>(extraInfo))
                .read(false)
                .status(true)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build();

        return NotificationAggregate.from(entity);
    }

    /**
     * 自定义通知处理
     * 子类可以覆盖此方法来添加额外的处理逻辑
     */
    protected void customizeNotification(NotificationAggregate notification) {
        // 默认实现为空
    }

    /**
     * 添加额外信息
     */
    protected void addExtraInfo(String key, Object value) {
        if (key != null && value != null) {
            this.extraInfo.put(key, value);
        }
    }

    /**
     * 验证数据
     */
    public abstract void validate();
} 