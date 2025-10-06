package cn.xu.domain.user.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户更新事件
 */
@Data
@Builder
public class UserUpdatedEvent {
    private Long userId;
    private String username;
    private LocalDateTime updateTime;
}