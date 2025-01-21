package cn.xu.api.web.model.dto.user;

import lombok.Data;

@Data
public class LoginRequest {
    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;
}
