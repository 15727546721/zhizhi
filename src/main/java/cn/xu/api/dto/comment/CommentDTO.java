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
public class CommentDTO {
    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论类型，如1-文章；2-话题
     */
    private Integer type;

    /**
     * 评论来源的标识符
     */
    private Long targetId;

    /**
     * 父评论的唯一标识符，顶级评论为NULL
     */
    private Long parentId;

    /**
     * 发表评论的用户ID
     */
    private Long userId;

    /**
     * 发表评论的用户名称
     */
    private String nickname;

    /**
     * 发表评论的用户头像
     */
    private String avatar;

    /**
     * 回复的用户ID，若为回复评论则存在
     */
    private Long replyUserId;

    /**
     * 评论的具体内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 