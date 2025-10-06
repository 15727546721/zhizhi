package cn.xu.domain.user.model.vo;

import lombok.Data;

@Data
public class UserFormResponse {
    private Long id;
    private String username;
    private String password;
}