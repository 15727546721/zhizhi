package cn.xu.api.web.model.vo.comment;

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
public class CommentWithPreviewResponse {

    private Long id;
    private Integer type;
    private Long targetId;
    private Long parentId;

    private Long userId;
    private String nickname;
    private String avatar;

    private String content;
    private Long replyUserId;

    private Integer childCount;
    private List<CommentSimpleResponse> previewReplies; // 简化子评论结构

    private List<String> imageUrls;
    private LocalDateTime createTime;
}