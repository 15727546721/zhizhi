package cn.xu.model.dto.user;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private Integer gender;
    private String phone;
    private String region;
    private String birthday;
    private String description;
}