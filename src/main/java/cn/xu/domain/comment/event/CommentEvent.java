package cn.xu.domain.comment.event;

import cn.xu.domain.comment.model.valueobject.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEvent {
    private Long commentId;
    private CommentType targetType;
    private Long targetId;
    private String content;
    private Long userId;
    private Long replyUserId;

    /**
     * 评论附带图片链接列表
     */
    private List<String> imageUrls;
}
