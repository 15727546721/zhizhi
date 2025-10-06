package cn.xu.api.web.model.vo.comment;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 热点评论Response
 * 用于热点评论接口返回数据
 * 
 * @author Lily
 */
@Data
public class HotCommentResponse {

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
     * 发表评论的用户ID
     */
    private Long userId;

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
     * 热度分数
     */
    private Double hotScore;

    /**
     * 是否热门评论
     */
    private Boolean isHot;

    /**
     * 图片URL列表
     */
    private List<String> imageUrls;

    /**
     * 评论用户信息
     */
    private CommentUserResponse user;

    /**
     * 被回复用户信息
     */
    private CommentUserResponse replyUser;

    /**
     * 子评论列表
     */
    private List<HotCommentResponse> children;
}