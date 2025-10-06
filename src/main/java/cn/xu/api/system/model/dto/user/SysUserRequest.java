package cn.xu.api.system.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统用户请求参数")
public class SysUserRequest {
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * 性别（0-女，1-男）
     */
    @Schema(description = "性别（0-女，1-男）")
    private Integer gender;

    /**
     * 手机
     */
    @Schema(description = "手机")
    private String phone;

    /**
     * 地区
     */
    @Schema(description = "地区")
    private String region;

    /**
     * 生日
     */
    @Schema(description = "生日")
    private String birthday;

    /**
     * 用户状态（0：正常，1：封禁）
     */
    @Schema(description = "用户状态（0：正常，1：封禁）")
    private Integer status;

    /**
     * 个人介绍
     */
    @Schema(description = "个人介绍")
    private String description;
}