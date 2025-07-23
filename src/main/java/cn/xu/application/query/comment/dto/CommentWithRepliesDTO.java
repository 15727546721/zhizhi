package cn.xu.application.query.comment.dto;

import lombok.Data;

import java.util.List;

@Data
public class CommentWithRepliesDTO extends CommentDTO {
    private List<CommentDTO> previewReplies;
    private Long replyCount;
}
