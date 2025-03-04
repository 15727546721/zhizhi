package cn.xu.api.web.model.vo.notification;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Schema(description = "通知视图对象")
public class NotificationVO {
    @Schema(description = "通知ID")
    private Long id;

    @Schema(description = "通知类型")
    private NotificationType type;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者名称")
    private String senderName;

    @Schema(description = "发送者头像")
    private String senderAvatar;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "业务类型")
    private BusinessType notificationBusinessType;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "额外数据")
    private Map<String, Object> extraInfo;

    @Schema(description = "是否已读")
    private Boolean read;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    public static NotificationVO fromAggregate(NotificationAggregate aggregate) {
        NotificationVO vo = new NotificationVO();
        vo.setId(aggregate.getId());
        vo.setType(aggregate.getType());
        vo.setSenderId(aggregate.getSenderId());
        vo.setTitle(aggregate.getTitle());
        vo.setContent(aggregate.getContent());
        vo.setNotificationBusinessType(aggregate.getBusinessType());
        vo.setBusinessId(aggregate.getBusinessId());
        vo.setRead(aggregate.isRead());
        vo.setCreatedTime(aggregate.getCreateTime());
        vo.setUpdatedTime(aggregate.getNotification().getUpdateTime());

        return vo;
    }
} 