package cn.xu.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户VO（View Object）
 * 用于表示用户的视图对象，包含用户相关信息的展示。
 */
@Data
@Builder
public class UserVO {

    private Long id;
    private String username;

    @Schema(description = "昵称（可修改）", example = "张三")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "个人简介", example = "热爱编程，专注Java开发")
    private String description;

    // ========== 用户类型 ==========

    @Schema(description = "用户类型：1-普通用户 2-官方账号 3-管理员", example = "1")
    private Integer userType;

    @Schema(description = "账号状态：0-禁用 1-正常 2-待审核", example = "1")
    private Integer status;

    // ========== 统计信息 ==========

    @Schema(description = "关注数", example = "100")
    private Long followCount;

    @Schema(description = "粉丝数", example = "500")
    private Long fansCount;

    @Schema(description = "获赞总数", example = "1000")
    private Long likeCount;

    @Schema(description = "发帖数", example = "50")
    private Long postCount;

    @Schema(description = "评论数", example = "200")
    private Long commentCount;

    // ========== 用户交互状态（需要登录） ==========

    @Schema(description = "当前用户是否已关注", example = "false")
    private Boolean isFollowed;

    // ========== 时间信息 ==========

    @Schema(description = "注册时间", example = "2025-01-01T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "最后登录时间", example = "2025-11-24T10:00:00")
    private LocalDateTime lastLoginTime;

    // ========== 辅助方法 ==========

    /**
     * 设置用户名，并验证用户名不能为空。
     */
    public void setUsername(String username) {
        if (username != null && !username.isEmpty()) {
            this.username = username;
        } else {
            throw new IllegalArgumentException("用户名不能为空");
        }
    }

}