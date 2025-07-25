package cn.xu.domain.comment.model.entity;

import cn.xu.domain.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {
    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论类型，如1-文章；2-话题
     */
    private Integer targetType;

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

    private UserEntity user;
    private UserEntity replyUser;
    /**
     * 子评论列表
     */
    private List<CommentEntity> children;

}