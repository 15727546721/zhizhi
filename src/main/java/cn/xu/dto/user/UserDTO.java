package cn.xu.dto.user;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String status = "0";
}
