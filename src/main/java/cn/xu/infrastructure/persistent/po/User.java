package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    /**
     * 用户ID
     */
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码(加密后)
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
     * 性别(1:男 0:女)
     */
    private Integer gender;
    /**
     * 手机号
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
     * 个人简介
     */
    private String description;
    /**
     * 账号状态(1:正常 0:禁用)
     */
    private Integer status;
    /**
     * 关注数量
     */
    private Long followCount;
    /**
     * 粉丝数量
     */
    private Long fansCount;
    /**
     * 获赞数量
     */
    private Long likeCount;
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 帖子数量（临时字段，用于排行榜查询）
     */
    private Long postCount;
}