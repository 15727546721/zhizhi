package cn.xu.api.controller.web.user;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
