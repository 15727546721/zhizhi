package cn.xu.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新通知设置请求
 */
@Data
@Schema(description = "更新通知设置请求")
public class UpdateNotificationSettingsRequest {
    
    @Schema(description = "邮件通知", example = "true")
    private Boolean emailNotification;
    
    @Schema(description = "浏览器通知", example = "true")
    private Boolean browserNotification;
    
    @Schema(description = "消息提示音", example = "false")
    private Boolean soundNotification;
}

