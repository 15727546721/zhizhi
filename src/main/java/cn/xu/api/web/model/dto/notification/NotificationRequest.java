package cn.xu.api.web.model.dto.notification;

import cn.xu.common.request.PageRequest;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知查询请求参数")
public class NotificationRequest extends PageRequest {

    @Schema(description = "通知类型")
    private NotificationType type;

    @Schema(description = "是否只查询未读", defaultValue = "false")
    private Boolean unreadOnly = false;
} 