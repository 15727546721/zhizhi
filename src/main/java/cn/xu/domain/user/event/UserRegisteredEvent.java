package cn.xu.domain.user.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户注册事件
 */
@Data
@Builder
public class UserRegisteredEvent {
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime registerTime;
}