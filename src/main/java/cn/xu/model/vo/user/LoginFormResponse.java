package cn.xu.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginFormResponse {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}