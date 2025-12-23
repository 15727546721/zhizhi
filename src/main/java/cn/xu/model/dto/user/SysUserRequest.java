package cn.xu.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户请求参数
 */
@Data
@Schema(description = "用户请求参数")
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
     * 性别：0-保密，1-男，2-女
     */
    @Schema(description = "性别：0-保密，1-男，2-女")
    private Integer gender;

    /**
     * 电话
     */
    @Schema(description = "电话")
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
     * 用户状态：0-禁用，1-启用
     */
    @Schema(description = "用户状态：0-禁用，1-启用")
    private Integer status;

    /**
     * 用户描述
     */
    @Schema(description = "用户描述")
    private String description;

    /**
     * 用户类型：1-普通用户，2-管理员
     */
    @Schema(description = "用户类型：1-普通用户，2-管理员")
    private Integer userType;
}
