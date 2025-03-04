package cn.xu.domain.user.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginFormVO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
