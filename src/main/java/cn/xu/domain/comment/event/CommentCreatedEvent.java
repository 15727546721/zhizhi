package cn.xu.domain.comment.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用于评论创建/回复
 */
@Data
@Builder
public class CommentCreatedEvent {
    private Long commentId;
    private Integer targetType;
    private Long targetId;
    private Long parentId;
    private Long userId;
    private Long replyUserId;
    private String content;
    private LocalDateTime createTime;

    private List<String> imageUrls; // 评论图片URL列表
}
