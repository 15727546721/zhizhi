package cn.xu.model.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 保存评论请求DTO（内部使用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveCommentRequest {

    /** 用户ID */
    private Long userId;

    /** 目标类型：1-帖子评论 */
    private Integer targetType;

    /** 目标ID（帖子ID） */
    private Long targetId;

    /** 评论内容 */
    private String content;

    /** 父评论ID，根评论为null */
    private Long parentId;

    /** 被回复的用户ID */
    private Long replyUserId;

    /** 评论图片URL列表 */
    private List<String> imageUrls;

    /** @提及的用户ID列表 */
    private List<Long> mentionedUserIds;
}
