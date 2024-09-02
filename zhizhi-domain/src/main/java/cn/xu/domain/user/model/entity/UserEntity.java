package cn.xu.domain.user.model.entity;

import cn.xu.types.model.PageResponse;
import lombok.Data;

@Data
public class UserEntity extends PageResponse {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private String status;
    private String createTime;
    private String updateTime;
}
