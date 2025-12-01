package cn.xu.model.vo.notification;

import cn.xu.model.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知响应
 *
 * @author xu
 */
@Data
@Schema(description = "通知响应对象")
public class NotificationResponse {
    
    @Schema(description = "通知ID")
    private Long id;

    @Schema(description = "通知类型：0-系统 1-点赞 2-收藏 3-评论 4-回复 5-关注")
    private Integer type;

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

    @Schema(description = "业务类型：0-系统 1-帖子 2-评论 3-用户")
    private Integer businessType;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "是否已读")
    private Boolean read;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    /**
     * 从 Notification PO 转换
     */
    public static NotificationResponse from(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setSenderId(notification.getSenderId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setBusinessType(notification.getBusinessType());
        response.setBusinessId(notification.getBusinessId());
        response.setRead(notification.getIsRead() != null && notification.getIsRead() == 1);
        response.setCreatedTime(notification.getCreateTime());
        response.setUpdatedTime(notification.getUpdateTime());
        return response;
    }
}