package cn.xu.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户基本信息VO
 * 用于用户基本信息展示
 * 
 * 使用场景：
 * - 帖子作者信息
 * - 评论用户信息
 * - 用户列表
 * - 用户搜索结果
 * - 关注/粉丝列表
 * 
 * @author zhizhi
 * @since 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户基本信息VO")
public class UserVO {
    
    // ========== 基础信息 ==========
    
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Schema(description = "用户名（唯一，不可修改）", example = "zhangsan_123")
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
     * 判断是否为普通用户
     */
    public boolean isNormalUser() {
        return userType != null && userType == 1;
    }
    
    /**
     * 判断是否为官方账号
     */
    public boolean isOfficialAccount() {
        return userType != null && userType == 2;
    }
    
    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return userType != null && userType == 3;
    }
    
    /**
     * 判断账号是否正常
     */
    public boolean isActive() {
        return status != null && status == 1;
    }
    
    /**
     * 判断账号是否被禁用
     */
    public boolean isDisabled() {
        return status != null && status == 0;
    }
}
