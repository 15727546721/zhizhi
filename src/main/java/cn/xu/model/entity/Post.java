package cn.xu.model.entity;

import cn.xu.common.ResponseCode;
import cn.xu.support.exception.BusinessException;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子PO (V3.0 - 简化重构版)
 * 
 * 架构原则:
 * 1. 充血模型：PO直接包含业务逻辑方法
 * 2. 0次转换：直接使用PO，无需Entity/Aggregate
 * 3. 简化验证：业务规则内聚在PO中
 * 4. 数据库映射：与表结构完全一致
 *
 * @TableName post
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Post implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ==================== 状态常量 ====================
    
    public static final int STATUS_DRAFT = 0;       // 草稿
    public static final int STATUS_PUBLISHED = 1;   // 已发布
    public static final int STATUS_DELETED = 2;     // 已删除
    public static final int STATUS_ARCHIVED = 3;    // 已归档
    
    // ==================== 类型常量（已废弃，使用Tag系统替代） ====================
    // 所有帖子统一为POST类型，通过标签(Tag)来分类
    
    // ==================== 字段定义 ====================
    
    private Long id;                    // 帖子ID
    private String title;               // 标题
    private String description;         // 简介
    private String content;             // 内容
    private String coverUrl;            // 封面URL
    private Long userId;                // 作者ID
    private Integer isFeatured;         // 是否加精：0-否，1-是
    private Long viewCount;             // 浏览量
    private Long favoriteCount;         // 收藏数
    private Long commentCount;          // 评论数
    private Long likeCount;             // 点赞数
    private Long shareCount;            // 分享数
    private Integer status;             // 状态
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
    
    // ==================== 工厂方法 ====================
    
    /**
     * 创建草稿帖子
     */
    public static Post createDraft(Long userId, String title, String content, String description) {
        if (userId == null) throw new IllegalArgumentException("用户ID不能为空");
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("帖子标题不能为空");
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("帖子内容不能为空");
        
        return Post.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .description(description)
                .status(STATUS_DRAFT)
                .isFeatured(0)
                .viewCount(0L)
                .favoriteCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .shareCount(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    // ==================== 生命周期方法 ====================
    
    /**
     * 发布帖子
     */
    public void publish() {
        if (this.status != null && this.status == STATUS_PUBLISHED) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子已发布");
        }
        if (this.status != null && this.status == STATUS_DELETED) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "已删除的帖子不能发布");
        }
        this.status = STATUS_PUBLISHED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 撤回帖子到草稿状态
     */
    public void withdraw() {
        if (this.status == null || this.status != STATUS_PUBLISHED) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "只有已发布的帖子才能撤回");
        }
        this.status = STATUS_DRAFT;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 删除帖子
     */
    public void delete() {
        if (this.status != null && this.status == STATUS_DELETED) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子已被删除");
        }
        this.status = STATUS_DELETED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 更新帖子内容
     */
    public void updateContent(String title, String content, String description) {
        if (this.status != null && this.status == STATUS_PUBLISHED) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "已发布的帖子不能编辑，请先撤回到草稿状态");
        }
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (description != null) this.description = description;
        this.updateTime = LocalDateTime.now();
    }
    
    // ==================== 统计数据操作 ====================
    
    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }
    
    public void increaseLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }
    
    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, (this.likeCount == null ? 0 : this.likeCount) - 1);
    }
    
    public void increaseFavoriteCount() {
        this.favoriteCount = (this.favoriteCount == null ? 0 : this.favoriteCount) + 1;
    }
    
    public void decreaseFavoriteCount() {
        this.favoriteCount = Math.max(0, (this.favoriteCount == null ? 0 : this.favoriteCount) - 1);
    }
    
    public void increaseCommentCount() {
        this.commentCount = (this.commentCount == null ? 0 : this.commentCount) + 1;
    }
    
    public void decreaseCommentCount() {
        this.commentCount = Math.max(0, (this.commentCount == null ? 0 : this.commentCount) - 1);
    }
    
    public void increaseShareCount() {
        this.shareCount = (this.shareCount == null ? 0 : this.shareCount) + 1;
    }
    
    // ==================== 状态判断 ====================
    
    public boolean isPublished() {
        return this.status != null && this.status == STATUS_PUBLISHED;
    }
    
    public boolean isDraft() {
        return this.status != null && this.status == STATUS_DRAFT;
    }
    
    public boolean isDeleted() {
        return this.status != null && this.status == STATUS_DELETED;
    }
    
    // ==================== 加精操作 ====================
    
    public boolean isFeaturedPost() {
        return this.isFeatured != null && this.isFeatured == 1;
    }
    
    public void setFeaturedPost(boolean featured) {
        this.isFeatured = featured ? 1 : 0;
        this.updateTime = LocalDateTime.now();
    }
    
    // ==================== 权限验证 ====================
    
    /**
     * 验证用户是否有权限操作该帖子
     */
    public void validateOwnership(Long currentUserId, boolean isAdmin) {
        if (currentUserId == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户未登录");
        }
        if (isAdmin) {
            return; // 管理员有所有权限
        }
        if (!this.userId.equals(currentUserId)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "无权操作此帖子");
        }
    }
    
    /**
     * 验证帖子完整性（用于创建/发布前）
     */
    public void validateForCreation() {
        if (this.title == null || this.title.trim().isEmpty()) {
            throw new IllegalArgumentException("帖子标题不能为空");
        }
        if (this.title.length() > 100) {
            throw new IllegalArgumentException("帖子标题长度不能超过100个字符");
        }
        if (this.content == null || this.content.trim().isEmpty()) {
            throw new IllegalArgumentException("帖子内容不能为空");
        }
        if (this.content.length() > 50000) {
            throw new IllegalArgumentException("帖子内容长度不能超过50000个字符");
        }
        if (this.userId == null) {
            throw new IllegalArgumentException("帖子作者ID不能为空");
        }
        // type字段已废弃，不再验证
    }
}
