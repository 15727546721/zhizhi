package cn.xu.api.dto.request.user;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String status = "0";
}
