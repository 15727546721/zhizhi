package cn.xu.domain.comment.event;

import lombok.Data;

@Data
public class CommentEvent {
    private Long commentId;
    private Long targetId;
    private String content;

    /**
     * 发表评论的用户ID
     */
    private Long userId;

    /**
     * 回复的用户ID，若为回复评论则存在
     */
    private Long replyUserId;

}
