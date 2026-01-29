package cn.xu.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 验证邮箱请求
 */
@Data
@Schema(description = "验证邮箱请求")
public class VerifyEmailRequest {

    /**
     * 验证令牌
     */
    @NotBlank(message = "验证令牌不能为空")
    @Schema(description = "邮箱验证令牌", example = "abc123...", required = true)
    private String token;
}
