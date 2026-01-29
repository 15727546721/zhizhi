package cn.xu.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送邮箱验证请求

 */
@Data
@Schema(description = "发送邮箱验证请求")
public class SendEmailVerifyRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "user@example.com", required = true)
    private String email;
}
