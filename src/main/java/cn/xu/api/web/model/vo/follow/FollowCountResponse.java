package cn.xu.api.web.model.vo.follow;

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