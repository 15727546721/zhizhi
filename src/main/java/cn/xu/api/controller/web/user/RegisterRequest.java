package cn.xu.api.controller.web.user;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nickname;
    private String email;
    private String password;
    private String confirmPassword;
}
