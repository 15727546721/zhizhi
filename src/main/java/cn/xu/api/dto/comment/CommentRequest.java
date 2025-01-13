package cn.xu.api.dto.comment;

import cn.xu.api.dto.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "评论查询请求参数")
public class CommentRequest extends PageRequest {

    @Schema(description = "评论类型")
    Integer type;
}
