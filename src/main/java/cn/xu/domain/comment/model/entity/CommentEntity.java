package cn.xu.domain.comment.model.entity;

import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

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
     * 回复的具体评论ID
     */
    private Long replyCommentId;

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

    /**
     * 子评论列表
     */
    private List<CommentEntity> children;

    /**
     * 验证评论数据
     */
    public void validate() {
        if (type == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论类型不能为空");
        }
        if (targetId == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论目标ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户ID不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论内容不能为空");
        }
        if (content.length() > 1000) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论内容不能超过1000字");
        }

    }
} 