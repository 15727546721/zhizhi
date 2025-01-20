package cn.xu.api.web.model.dto.user;

import lombok.Data;

@Data
public class UserPasswordRequest {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}
