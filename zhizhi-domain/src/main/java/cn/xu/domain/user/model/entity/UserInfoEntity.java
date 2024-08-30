package cn.xu.domain.user.model.entity;

import lombok.Data;

import java.util.Set;

/**
 * 用户信息
 */
@Data
public class UserInfoEntity {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private Set<String> roles;
    private String email;
    private String avatar;
    private String status;
}
