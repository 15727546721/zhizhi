package cn.xu.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 注销账户请求
 */
@Data
@Schema(description = "注销账户请求")
public class DeleteAccountRequest {
    
    @NotBlank(message = "密码不能为空")
    @Schema(description = "用户密码（用于确认身份）", example = "password123", required = true)
    private String password;
}
