package cn.xu.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoEntity {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String phone;
    private String region;
    private String birthday;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> roles;
}
