package cn.xu.model.vo.post;

import cn.xu.model.vo.tag.TagVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子列表项VO
 * 用于帖子列表展示，包含必要的展示字段
 * 
 * 使用场景：
 * - 首页帖子列表
 * - 分类/标签帖子列表
 * - 用户帖子列表
 * - 搜索结果列表
 * 
 * @author zhizhi
 * @since 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子列表项VO")
public class PostItemVO {
    
    // ========== 基础信息 ==========
    
    @Schema(description = "帖子ID", example = "1")
    private Long id;
    
    @Schema(description = "帖子标题", example = "如何学习Java")
    private String title;
    
    @Schema(description = "帖子描述/摘要", example = "本文介绍Java学习路线...")
    private String description;
    
    @Schema(description = "帖子内容（摘要）", example = "本文详细介绍...")
    private String content;
    
    @Schema(description = "封面图URL", example = "https://example.com/cover.jpg")
    private String coverUrl;
    
    @Schema(description = "帖子状态", example = "1",
            allowableValues = {"0", "1", "2"})
    private Integer status;
    
    // ========== 作者信息 ==========
    
    @Schema(description = "作者ID", example = "1")
    private Long userId;
    
    @Schema(description = "作者昵称", example = "张三")
    private String nickname;
    
    @Schema(description = "作者头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    @Schema(description = "用户类型：1-普通用户 2-官方账号 3-管理员", example = "1")
    private Integer userType;
    
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
    
    @Schema(description = "标签名称数组", example = "[\"Java\", \"后端\"]")
    private String[] tagNameList;
    
    // ========== 时间信息 ==========
    
    @Schema(description = "创建时间", example = "2025-11-24T10:00:00")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间", example = "2025-11-24T12:00:00")
    private LocalDateTime updateTime;
    
    @Schema(description = "发布时间", example = "2025-11-24T11:00:00")
    private LocalDateTime publishTime;
    
    // ========== 特殊标识 ==========
    
    @Schema(description = "是否精选", example = "false")
    private Boolean isFeatured;
    
    @Schema(description = "是否置顶", example = "false")
    private Boolean isTop;
    
    // ========== 辅助方法 ==========
    
    /**
     * 从标签列表生成标签名称数组
     */
    public void generateTagNameList() {
        if (tags != null && !tags.isEmpty()) {
            this.tagNameList = tags.stream()
                    .map(TagVO::getName)
                    .toArray(String[]::new);
        }
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
