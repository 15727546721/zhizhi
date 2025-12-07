package cn.xu.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 验证码登录请求
 *
 * @author xu
 * @since 2024-12-07
 */
@Data
@Schema(description = "验证码登录请求")
public class CodeLoginRequest {

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", required = true, example = "user@example.com")
    private String email;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码为6位数字")
    @Schema(description = "验证码", required = true, example = "123456")
    private String verifyCode;
}
