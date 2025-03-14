package cn.xu.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer gender;
    private String phone;
    private String region;
    private String birthday;
    private String description;
    private Integer followCount; // 关注数量
    private Integer fansCount;   // 粉丝数量
    private Integer likeCount;   // 获赞数量
    private LocalDateTime lastLoginTime; // 最后登录时间
    private String lastLoginIp; // 最后登录IP
}
