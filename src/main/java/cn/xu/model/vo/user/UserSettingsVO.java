package cn.xu.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户设置VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户设置")
public class UserSettingsVO {
    
    @Schema(description = "隐私设置")
    private PrivacySettingsVO privacySettings;
    
    @Schema(description = "通知设置")
    private NotificationSettingsVO notificationSettings;
    
    @Schema(description = "邮箱是否已验证")
    private Boolean emailVerified;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "隐私设置")
    public static class PrivacySettingsVO {
        @Schema(description = "个人资料可见性: 1-公开 2-仅关注者可见 3-私密", example = "1")
        private Integer profileVisibility;
        
        @Schema(description = "是否显示在线状态", example = "true")
        private Boolean showOnlineStatus;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "通知设置")
    public static class NotificationSettingsVO {
        @Schema(description = "邮件通知", example = "true")
        private Boolean emailNotification;
        
        @Schema(description = "浏览器通知", example = "true")
        private Boolean browserNotification;
        
        @Schema(description = "消息提示音", example = "false")
        private Boolean soundNotification;
    }
}

