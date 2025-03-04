package cn.xu.api.web.model.vo.follow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "关注状态响应")
public class FollowStatusVO {

    @Schema(description = "是否关注")
    private Boolean isFollowing;
} 