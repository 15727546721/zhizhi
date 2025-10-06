package cn.xu.domain.user.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录事件
 */
@Data
@Builder
public class UserLoggedInEvent {
    private Long userId;
    private String username;
    private LocalDateTime loginTime;
}