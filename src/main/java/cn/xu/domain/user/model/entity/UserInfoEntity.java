package cn.xu.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 用户信息
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
