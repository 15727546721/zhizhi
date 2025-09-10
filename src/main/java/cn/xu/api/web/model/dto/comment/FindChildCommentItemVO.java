package cn.xu.api.web.model.dto.comment;

import cn.xu.domain.user.model.entity.UserEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 查询(二级评论)子评论返回的VO
 */
@Data
public class FindChildCommentItemVO {
    /**
     * 评论 ID
     */
    private Long id;

    /**
     * 发布者
     */
    private UserEntity user;
    /**
     * 回复用户
     */
    private UserEntity replyUser;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 评论时间
     */
    private LocalDateTime createTime;
    /**
     * 点赞数量
     */
    private Integer likeCount;
}
