package cn.xu.model.vo.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知VO
 * 用于通知信息展示
 * 
 * 使用场景：
 * - 通知列表
 * - 未读通知
 * - 系统通知
 * 
 * @author zhizhi
 * @since 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知VO")
public class NotificationVO {
    
    // ========== 基础信息 ==========
    
    @Schema(description = "通知ID", example = "1")
    private Long id;
    
    @Schema(description = "接收者用户ID", example = "1")
    private Long receiverId;
    
    @Schema(description = "发送者用户ID", example = "2")
    private Long senderId;
    
    @Schema(description = "发送者昵称", example = "张三")
    private String senderNickname;
    
    @Schema(description = "发送者头像", example = "https://example.com/avatar.jpg")
    private String senderAvatar;
    
    // ========== 通知内容 ==========
    
    @Schema(description = "通知类型", example = "LIKE",
            allowableValues = {"LIKE", "COMMENT", "FOLLOW", "SYSTEM", "REPLY", "MENTION"})
    private String type;
    
    @Schema(description = "通知标题", example = "张三点赞了你的帖子")
    private String title;
    
    @Schema(description = "通知内容", example = "张三点赞了你的帖子《如何学习Java》")
    private String content;
    
    // ========== 关联信息 ==========
    
    @Schema(description = "关联目标类型", example = "POST",
            allowableValues = {"POST", "COMMENT", "USER"})
    private String targetType;
    
    @Schema(description = "关联目标ID", example = "100")
    private Long targetId;
    
    @Schema(description = "关联目标标题/名称", example = "如何学习Java")
    private String targetTitle;
    
    // ========== 状态信息 ==========
    
    @Schema(description = "是否已读", example = "false")
    private Boolean isRead;
    
    @Schema(description = "阅读时间", example = "2025-11-24T12:00:00")
    private LocalDateTime readTime;
    
    // ========== 时间信息 ==========
    
    @Schema(description = "创建时间", example = "2025-11-24T10:00:00")
    private LocalDateTime createTime;
}
