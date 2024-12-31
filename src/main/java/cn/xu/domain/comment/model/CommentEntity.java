package cn.xu.domain.comment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {
    private Long id;
    private Long userId;
    private String content;
    private Long parentId;
    private Long replyToUserId;
    private Date createTime;
    private Date updateTime;
    private List<CommentEntity> children;
}
