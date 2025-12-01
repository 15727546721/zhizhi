package cn.xu.model.vo.post;

import cn.xu.model.vo.comment.CommentVO;
import cn.xu.model.vo.tag.TagVO;
import cn.xu.model.vo.user.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子详情VO
 * 用于帖子详情页展示，包含完整的帖子信息和关联数据
 * 
 * 使用场景：
 * - 帖子详情页
 * - 帖子编辑页回显
 * 
 * @author zhizhi
 * @since 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子详情VO")
public class PostDetailVO {
    
    // ========== 基础信息（与PostItemVO一致） ==========
    
    @Schema(description = "帖子ID", example = "1")
    private Long id;
    
    @Schema(description = "帖子标题", example = "如何学习Java")
    private String title;
    
    @Schema(description = "帖子描述/摘要", example = "本文介绍Java学习路线...")
    private String description;
    
    @Schema(description = "帖子内容（原始）")
    private String content;
    
    @Schema(description = "帖子内容（HTML格式）")
    private String contentHtml;
    
    @Schema(description = "帖子类型", example = "ARTICLE",
            allowableValues = {"POST", "ARTICLE", "DISCUSSION", "QUESTION"})
    private String type;
    
    @Schema(description = "封面图URL", example = "https://example.com/cover.jpg")
    private String coverUrl;
    
    @Schema(description = "帖子状态", example = "1")
    private Integer status;
    
    // ========== 作者完整信息 ==========
    
    @Schema(description = "作者信息")
    private UserVO author;
    
    // ========== 统计信息 ==========
    
    @Schema(description = "浏览数", example = "1000")
    private Long viewCount;
    
    @Schema(description = "点赞数", example = "100")
    private Long likeCount;
    
    @Schema(description = "评论数", example = "50")
    private Long commentCount;
    
    @Schema(description = "收藏数", example = "30")
    private Long favoriteCount;
    
    @Schema(description = "分享数", example = "10")
    private Long shareCount;
    
    // ========== 标签信息 ==========
    
    @Schema(description = "标签列表")
    private List<TagVO> tags;
    
    // ========== 时间信息 ==========
    
    @Schema(description = "创建时间", example = "2025-11-24T10:00:00")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间", example = "2025-11-24T12:00:00")
    private LocalDateTime updateTime;
    
    @Schema(description = "发布时间", example = "2025-11-24T11:00:00")
    private LocalDateTime publishTime;
    
    // ========== 用户交互状态（需要登录） ==========
    
    @Schema(description = "当前用户是否已点赞", example = "false")
    private Boolean isLiked;
    
    @Schema(description = "当前用户是否已收藏", example = "false")
    private Boolean isFavorited;
    
    @Schema(description = "当前用户是否已关注作者", example = "false")
    private Boolean isFollowed;
    
    // ========== 特殊标识 ==========
    
    @Schema(description = "是否精选", example = "false")
    private Boolean isFeatured;
    
    @Schema(description = "是否置顶", example = "false")
    private Boolean isTop;
    
    // ========== 关联数据（可选） ==========
    
    @Schema(description = "热门评论列表（Top 5）")
    private List<CommentVO> topComments;
    
    @Schema(description = "相关推荐帖子列表（Top 5）")
    private List<PostItemVO> relatedPosts;
}
