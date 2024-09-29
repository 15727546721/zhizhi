package cn.xu.api.dto.user;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String status = "0";
}
