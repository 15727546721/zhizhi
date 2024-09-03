package cn.xu.api.model.user;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String status = "0";
}
