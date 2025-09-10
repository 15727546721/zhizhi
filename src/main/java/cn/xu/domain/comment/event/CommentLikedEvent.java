package cn.xu.domain.comment.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 评论点赞事件
 */
@Getter
@RequiredArgsConstructor
public class CommentLikedEvent {
    private final Long commentId;  // 被点赞评论ID
    private final Long userId;     // 点赞用户ID
    private final boolean isLike;  // true-点赞，false-取消点赞
}