package cn.xu.domain.comment.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论更新事件
 */
@Data
@Builder
public class CommentUpdatedEvent {
    private Long commentId;
    private Long userId;
    private String content;
    private LocalDateTime updateTime;
}