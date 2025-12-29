package cn.xu.model.vo.comment;

import cn.xu.model.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 * 表示单个评论的数据结构，包含评论的基本信息
 *
 * 使用场景:
 * - 评论列表
 * - 评论详情
 * - 评论回复
 * - 垂直评论
 * - 热门评论（例如，在评论区进行的点赞、回复等）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论VO")
public class CommentVO {

    // ========== 基本信息 ==========

    @Schema(description = "评论ID", example = "1")
    private Long id;

    @Schema(description = "帖子ID", example = "100")
    private Long postId;

    @Schema(description = "评论内容", example = "评论内容示例，包含文本等")
    private String content;

    @Schema(description = "图片URL列表")
    private List<String> imageUrls;

    // ========== 评论的父子层级 ==========

    @Schema(description = "父评论ID，如果是顶级评论，则为0", example = "0")
    private Long parentId;

    @Schema(description = "回复的用户ID，如果没有回复，则为null", example = "5")
    private Long replyToUserId;

    @Schema(description = "回复的用户昵称，如果没有回复，则为null", example = "李四")
    private String replyToNickname;

    @Schema(description = "评论级别：0-顶级评论，1-一级回复，2-二级回复...", example = "1")
    private Integer level;

    // ========== 评论用户信息 ==========

    @Schema(description = "评论用户ID", example = "1")
    private Long userId;

    @Schema(description = "评论用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "评论用户头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "用户类型：1-普通用户，2-认证账号，3-管理员", example = "1")
    private Integer userType;

    // ========== 互动信息 ==========

    @Schema(description = "点赞数", example = "10")
    private Long likeCount;

    @Schema(description = "回复数", example = "5")
    private Long replyCount;

    // ========== 用户交互状态 ==========

    @Schema(description = "当前用户是否点赞", example = "false")
    private Boolean isLiked;

    // ========== 评论状态 ==========

    @Schema(description = "是否置顶", example = "false")
    private Boolean isTop;

    @Schema(description = "是否热门评论", example = "false")
    private Boolean isHot;

    @Schema(description = "是否作者评论", example = "false")
    private Boolean isAuthor;

    @Schema(description = "评论状态：0-正常，1-删除，2-审核中", example = "1")
    private Integer status;

    // ========== 时间信息 ==========

    @Schema(description = "创建时间", example = "2025-11-24T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-24T12:00:00")
    private LocalDateTime updateTime;

    // ========== 热门评论（回复等） ==========

    @Schema(description = "回复列表：一个评论下的所有回复")
    private List<CommentVO> replies;

    @Schema(description = "是否还有更多回复", example = "true")
    private Boolean hasMoreReplies;

    // ========== 辅助方法 ==========

    /**
     * 判断是否是顶级评论
     */
    public boolean isTopLevel() {
        return parentId == null || parentId == 0;
    }

    /**
     * 判断是否是官方账号评论
     */
    public boolean isOfficialAccount() {
        return UserType.isOfficial(userType);
    }

    /**
     * 判断是否是管理员评论
     */
    public boolean isAdmin() {
        return UserType.isSuperAdmin(userType);
    }
}