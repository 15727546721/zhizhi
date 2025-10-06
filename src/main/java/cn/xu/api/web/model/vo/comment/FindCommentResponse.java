package cn.xu.api.web.model.vo.comment;

import cn.xu.domain.comment.model.entity.CommentEntity;
import lombok.Data;

@Data
public class FindCommentResponse {
    private CommentEntity comment;
}