package cn.xu.domain.user.model.valobj;

import lombok.Data;

@Data
public class LoginFormVO {
    private Long id;
    private String username;
    private String password;
    private String role;

    public LoginFormVO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
