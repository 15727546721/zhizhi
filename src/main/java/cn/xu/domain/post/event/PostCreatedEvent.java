package cn.xu.domain.post.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子创建事件
 */
@Data
@Builder
public class PostCreatedEvent {
    private Long postId;
    private Long userId;
    private String title;
    private LocalDateTime createTime;
}