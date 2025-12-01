package cn.xu.event.user;

import cn.xu.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户注册事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime registerTime;
    
    /**
     * 从User对象构建事件
     */
    public UserRegisteredEvent(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.registerTime = LocalDateTime.now();
    }
}