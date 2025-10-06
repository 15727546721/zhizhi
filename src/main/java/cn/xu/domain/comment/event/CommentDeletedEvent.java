package cn.xu.domain.comment.event;

import lombok.Builder;
import lombok.Data;

/**
 * 评论删除事件
 */
@Data
@Builder
public class CommentDeletedEvent {
    private final Long commentId;     // 被删除评论ID
    private final Integer targetType; // 目标类型
    private final Long targetId;      // 目标ID
    private final boolean isRootComment; // 是否一级评论
    private final Long parentId;      // 父评论ID（用于非根评论）
}