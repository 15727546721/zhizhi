package cn.xu.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserRoleEntity {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private String status;
    private Long roleId;
}
