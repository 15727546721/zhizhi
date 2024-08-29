package cn.xu.domain.user.model.entity;

import lombok.Data;

@Data
public class UserEntity {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String role;
    private String email;
    private String avatar;
    private String status;
    private String createTime;
    private String updateTime;
}
