package cn.xu.domain.post.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子更新事件
 */
@Data
@Builder
public class PostUpdatedEvent {
    private Long postId;
    private Long userId;
    private String title;
    private LocalDateTime updateTime;
}