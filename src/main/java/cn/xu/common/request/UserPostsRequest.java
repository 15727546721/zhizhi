package cn.xu.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户帖子列表请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户帖子列表请求参数")
public class UserPostsRequest extends PageRequest {

    @Schema(description = "帖子状态: PUBLISHED-已发布, DRAFT-草稿", defaultValue = "PUBLISHED")
    private String status = "PUBLISHED";
}
