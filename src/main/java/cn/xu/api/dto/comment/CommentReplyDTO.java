package cn.xu.api.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentReplyDTO {
    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 评论用户昵称
     */
    private String nickName;

    /**
     * 评论用户头像
     */
    private String avatar;

    /**
     * 被回复用户ID
     */
    private Long replyUserId;

    /**
     * 被回复用户昵称
     */
    private String replyNickname;

    /**
     * 被回复用户头像
     */
    private String replyAvatar;

    /**
     * 评论时间
     */
    private LocalDateTime createTime;
}