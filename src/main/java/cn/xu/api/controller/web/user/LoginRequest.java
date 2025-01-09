package cn.xu.api.controller.web.user;

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
