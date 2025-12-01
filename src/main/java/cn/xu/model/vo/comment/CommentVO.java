package cn.xu.model.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 * 统一的评论响应对象，覆盖所有评论场景
 * 
 * 使用场景：
 * - 评论列表
 * - 评论详情
 * - 热门评论
 * - 置顶评论
 * - 子评论（楼中楼）
 * 
 * @author zhizhi
 * @since 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论VO")
public class CommentVO {
    
    // ========== 基础信息 ==========
    
    @Schema(description = "评论ID", example = "1")
    private Long id;
    
    @Schema(description = "帖子ID", example = "100")
    private Long postId;
    
    @Schema(description = "评论内容", example = "写得很好，学到了！")
    private String content;
    
    // ========== 评论层级关系 ==========
    
    @Schema(description = "父评论ID（0表示顶级评论）", example = "0")
    private Long parentId;
    
    @Schema(description = "回复的用户ID（顶级评论时为null）", example = "5")
    private Long replyToUserId;
    
    @Schema(description = "回复的用户昵称（顶级评论时为null）", example = "李四")
    private String replyToNickname;
    
    @Schema(description = "评论层级（1-顶级，2-二级，3-三级...）", example = "1")
    private Integer level;
    
    // ========== 评论作者信息 ==========
    
    @Schema(description = "评论作者ID", example = "1")
    private Long userId;
    
    @Schema(description = "评论作者昵称", example = "张三")
    private String nickname;
    
    @Schema(description = "评论作者头像", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    @Schema(description = "用户类型：1-普通用户 2-官方账号 3-管理员", example = "1")
    private Integer userType;
    
    // ========== 统计信息 ==========
    
    @Schema(description = "点赞数", example = "10")
    private Long likeCount;
    
    @Schema(description = "子评论数量", example = "5")
    private Long replyCount;
    
    // ========== 用户交互状态 ==========
    
    @Schema(description = "当前用户是否已点赞", example = "false")
    private Boolean isLiked;
    
    // ========== 特殊标识 ==========
    
    @Schema(description = "是否置顶", example = "false")
    private Boolean isTop;
    
    @Schema(description = "是否热门评论", example = "false")
    private Boolean isHot;
    
    @Schema(description = "是否作者本人评论", example = "false")
    private Boolean isAuthor;
    
    @Schema(description = "评论状态：0-待审核 1-正常 2-已删除", example = "1")
    private Integer status;
    
    // ========== 时间信息 ==========
    
    @Schema(description = "创建时间", example = "2025-11-24T10:00:00")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间", example = "2025-11-24T12:00:00")
    private LocalDateTime updateTime;
    
    // ========== 子评论列表（楼中楼） ==========
    
    @Schema(description = "子评论列表（最多显示3条）")
    private List<CommentVO> replies;
    
    @Schema(description = "是否还有更多子评论", example = "true")
    private Boolean hasMoreReplies;
    
    // ========== 辅助方法 ==========
    
    /**
     * 判断是否为顶级评论
     */
    public boolean isTopLevel() {
        return parentId == null || parentId == 0;
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
}
