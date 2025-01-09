package cn.xu.api.dto.user;

import lombok.Data;

@Data
public class UserRequest {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别（0-女，1-男）
     */
    private Integer gender;

    /**
     * 手机
     */
    private String phone;

    /**
     * 地区
     */
    private String region;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 用户状态（0：正常，1：封禁）
     */
    private Integer status;

    /**
     * 个人介绍
     */
    private String description;
}
