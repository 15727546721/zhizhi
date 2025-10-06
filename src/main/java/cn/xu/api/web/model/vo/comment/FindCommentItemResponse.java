package cn.xu.api.web.model.vo.comment;

import cn.xu.domain.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *  查询评论分页数据
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentItemResponse {

    /**
     * 评论 ID
     */
    private Long id;

    /**
     * 发布者用户
     */
    private UserEntity user;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 发布时间
     */
    private String createTime;

    /**
     * 被点赞数
     */
    private Long likeCount;

    /**
     * 二级评论总数
     */
    private Long replyCount;

    /**
     * 最早回复的评论
     */
    private List<FindChildCommentItemResponse> replyComment;
}