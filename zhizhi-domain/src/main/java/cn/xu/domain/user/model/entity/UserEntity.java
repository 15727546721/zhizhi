package cn.xu.domain.user.model.entity;

import cn.xu.types.model.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserEntity extends PageResponse {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime  updateTime;
}
