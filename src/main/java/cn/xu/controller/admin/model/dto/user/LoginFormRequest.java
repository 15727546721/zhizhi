package cn.xu.controller.admin.model.dto.user;

import lombok.Data;

@Data
public class LoginFormRequest {
    private String username;
    private String password;
    private Boolean rememberMe;
}
