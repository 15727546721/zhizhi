package cn.xu.event.user;

import cn.xu.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户登录事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoggedInEvent {
    private Long userId;
    private String username;
    private LocalDateTime loginTime;
    
    /**
     * 从User对象构建事件
     */
    public UserLoggedInEvent(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.loginTime = LocalDateTime.now();
    }
}