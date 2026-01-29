package cn.xu.model.dto.like;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 点赞请求
 
 */
@Data
public class LikeRequest {

    /**
     * 用户ID（可选，后端会从登录上下文获取，前端不需要传递）
     */
    private Long userId;

    /**
     * 目标ID
     */
    @NotNull(message = "目标ID不能为空")
    @Positive(message = "目标ID必须大于0")
    private Long targetId;

    /**
     * 点赞类型
     * POST(1, "帖子"),
     * COMMENT(3, "评论");
     */
    @NotBlank(message = "点赞类型不能为空")
    private String type;
}
