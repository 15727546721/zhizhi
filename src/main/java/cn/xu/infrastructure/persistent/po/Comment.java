package cn.xu.infrastructure.persistent.po;

import cn.xu.domain.comment.model.valueobject.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论表，用于存储用户评论及其相关信息
 *
 * @TableName comment
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements Serializable {
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
     * 回复的用户ID，若为回复评论则存在
     */
    private Long replyUserId;

    /**
     * 评论的具体内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 子评论数
     */
    private Long replyCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public CommentType getCommentType() {
        return type != null ? CommentType.of(type) : null;
    }

    public void setCommentType(CommentType commentType) {
        this.type = commentType != null ? commentType.getValue() : null;
    }
}