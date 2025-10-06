package cn.xu.api.web.model.vo.follow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(description = "关注用户信息响应")
public class FollowUserResponse {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名称")
    private String username;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "关注时间")
    private LocalDateTime followTime;

    @Schema(description = "关注状态（0-取消关注，1-已关注）")
    private Integer status;
}