package cn.xu.api.web.model.dto.comment;

import cn.xu.api.web.model.vo.comment.CommentVO;
import cn.xu.domain.comment.model.entity.CommentEntity;
import lombok.Data;

@Data
public class FindCommentVO {
    private CommentEntity comment;
}
