package cn.xu.domain.comment.event;

import cn.xu.domain.comment.model.valueobject.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCountEvent {
    private Long commentId;
    private Long targetId;
    private CommentType targetType;
    private Integer level; // 评论层级
    private Integer count; // 评论数(可能是新增评论数，也可能是减少评论数)
}
