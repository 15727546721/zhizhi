package cn.xu.domain.comment.event;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentCreatedEvent {
    private Long id;
    private Integer targetType;
    private Long targetId;
    private Long parentId;
    private Long userId;
    private Long replyUserId;
    private String content;
    private LocalDateTime createTime;
}
