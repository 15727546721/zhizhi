package cn.xu.api.dto.user;

import lombok.Data;

@Data
public class UserPasswordRequest {
    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}
