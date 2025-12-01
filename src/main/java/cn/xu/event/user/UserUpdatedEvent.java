package cn.xu.event.user;

import cn.xu.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户更新事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {
    private Long userId;
    private String username;
    private LocalDateTime updateTime;
    
    /**
     * 从User对象构建事件
     */
    public UserUpdatedEvent(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.updateTime = LocalDateTime.now();
    }
}