package cn.xu.model.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 保存评论请求DTO
 * 用于表示用户发表评论时的请求数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveCommentRequest {

    /** 用户ID */
    private Long userId;

    /**
     * 目标类型
     * - 1: 帖子评论
     * - 2: 评论的评论（回复评论）
     */
    private Integer targetType;

    /**
     * 目标ID
     * - 如果是业务评论，则是业务目标ID
     * - 如果是评论的评论，则是原评论ID
     */
    private Long targetId;

    /** 评论内容 */
    private String content;

    /**
     * 父评论ID
     * - 表示回复哪个评论。如果是根评论，则为空。
     */
    private Long parentId;

    /**
     * 根评论ID
     * - 用于标识评论的根评论ID，特别用于处理多层嵌套评论。
     */
    private Long rootId;

    /**
     * 回复的用户ID
     * - 表示回复评论的用户ID
     */
    private Long replyUserId;

    /** 评论图片URL列表 */
    private List<String> imageUrls;

    /**
     * 是否是根评论
     * - 如果是根评论，值为 true，否则为 false
     */
    private Boolean isRootComment;

    /**
     * @提及的用户ID列表
     * - 评论中@的用户ID（仅限关注的用户）
     */
    private List<Long> mentionedUserIds;
}