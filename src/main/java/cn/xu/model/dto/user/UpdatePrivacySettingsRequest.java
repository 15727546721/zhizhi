package cn.xu.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 更新隐私设置请求
 
 */
@Data
@Schema(description = "更新隐私设置请求")
public class UpdatePrivacySettingsRequest {

    @Min(value = 1, message = "个人资料可见性值必须在1-3之间")
    @Max(value = 3, message = "个人资料可见性值必须在1-3之间")
    @Schema(description = "个人资料可见性: 1-公开, 2-仅关注者可见, 3-私密", example = "1")
    private Integer profileVisibility;

    @Schema(description = "是否显示在线状态", example = "true")
    private Boolean showOnlineStatus;
}
