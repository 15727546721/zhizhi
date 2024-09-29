package cn.xu.api.dto.user;

import lombok.Data;

@Data
public class LoginFormRequest {
    private String username;
    private String password;
}
