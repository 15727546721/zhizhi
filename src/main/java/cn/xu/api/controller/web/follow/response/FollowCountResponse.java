package cn.xu.api.controller.web.follow.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "关注数量响应")
public class FollowCountResponse {

    @Schema(description = "关注数量")
    private Integer count;

    @Schema(description = "用户ID")
    private Long userId;
} 