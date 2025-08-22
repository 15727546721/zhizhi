package cn.xu.domain.comment.event;

import cn.xu.domain.comment.model.valueobject.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 评论基础事件（用于评论创建/回复）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEvent {
    private Long commentId;       // 新评论ID（新增时可为空）
    private CommentType targetType; // 评论目标类型（文章/随笔等）
    private Long targetId;        // 目标ID（文章ID/随笔ID等）
    private Long parentId;        // 父评论ID（一级评论为null）
    private Long replyUserId;     // 被回复用户ID（直接回复目标时不为空）
    private String content;       // 评论内容
    private List<String> imageUrls; // 评论图片URL列表

    // 业务标识字段
    private Long userId;          // 评论用户ID（可从上下文获取，事件中携带便于处理）

}