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
    private String description;  // 添加描述字段，避免事件处理时查询数据库
    private LocalDateTime updateTime;
}