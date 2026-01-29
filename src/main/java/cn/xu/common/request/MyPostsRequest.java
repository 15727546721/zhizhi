package cn.xu.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 我的帖子列表请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "我的帖子列表请求参数")
public class MyPostsRequest extends PageRequest {

    @Schema(description = "帖子状态: PUBLISHED-已发布, DRAFT-草稿, 不传则查全部")
    private String status;

    @Schema(description = "搜索关键词")
    private String keyword;
}
