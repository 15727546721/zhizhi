package cn.xu.domain.post.model.entity;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.post.model.valobj.PostContent;
import cn.xu.domain.post.model.valobj.PostStatus;
import cn.xu.domain.post.model.valobj.PostTitle;
import cn.xu.domain.post.model.valobj.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 帖子领域实体
 * 封装帖子相关的业务逻辑和规则
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostEntity {
    private Long id;
    private Long categoryId;
    private PostTitle title;
    private String description;
    private PostContent content;
    private String coverUrl;
    private Long userId;
    private Long viewCount;
    private Long favoriteCount;
    private Long commentCount;
    private Long likeCount;
    private Long shareCount;
    private PostStatus status; // 帖子状态
    private PostType type; // 帖子类型
    private Boolean isFeatured; // 是否加精
    private Long acceptedAnswerId; // 被采纳的回答ID（仅用于问答帖）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime publishTime; // 发布时间

    // ==================== 构造方法 ====================
    
    /**
     * 创建新帖子草稿
     */
    public static PostEntity createDraft(Long userId, String title, String content, String description, Long categoryId, PostType type) {
        // 参数验证
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("帖子标题不能为空");
        }
        
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("帖子内容不能为空");
        }
        
        return PostEntity.builder()
                .userId(userId)
                .title(new PostTitle(title))
                .content(new PostContent(content))
                .description(description)
                .categoryId(categoryId)
                .type(type != null ? type : PostType.POST) // 默认为帖子类型
                .status(PostStatus.DRAFT) // 默认为草稿状态
                .isFeatured(false) // 默认不加精
                .viewCount(0L)
                .favoriteCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .shareCount(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    // ==================== 业务方法 ====================

    /**
     * 发布帖子
     */
    public void publish() {
        validateCanPublish();
        this.status = PostStatus.PUBLISHED;
        this.publishTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 撤回帖子（回到草稿状态）
     */
    public void withdraw() {
        if (PostStatus.PUBLISHED != this.status) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "只有已发布的帖子才能撤回");
        }
        this.status = PostStatus.DRAFT;
        this.publishTime = null;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 删除帖子
     */
    public void delete() {
        if (PostStatus.DELETED == this.status) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子已被删除");
        }
        this.status = PostStatus.DELETED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新帖子内容
     */
    public void updateContent(String title, String content, String description) {
        if (PostStatus.PUBLISHED == this.status) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "已发布的帖子不能编辑，请先撤回到草稿状态");
        }
        
        this.title = new PostTitle(title);
        this.content = new PostContent(content);
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置帖子封面
     */
    public void setCoverImage(String coverUrl) {
        if (coverUrl == null || coverUrl.trim().isEmpty()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "封面图片地址不能为空");
        }
        this.coverUrl = coverUrl;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 设置封面URL
     */
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    /**
     * 增加浏览量
     */
    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
        // 注意：浏览量的更新通常不需要更新updateTime，避免频繁的数据库写入
        // 根据业务需求，浏览量更新不修改更新时间
    }

    /**
     * 增加点赞数
     */
    public void increaseLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }

    /**
     * 减少点赞数
     */
    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, (this.likeCount == null ? 0 : this.likeCount) - 1);
    }

    /**
     * 增加收藏数
     */
    public void increaseFavoriteCount() {
        this.favoriteCount = (this.favoriteCount == null ? 0 : this.favoriteCount) + 1;
    }

    /**
     * 减少收藏数
     */
    public void decreaseFavoriteCount() {
        this.favoriteCount = Math.max(0, (this.favoriteCount == null ? 0 : this.favoriteCount) - 1);
    }

    /**
     * 增加评论数
     */
    public void increaseCommentCount() {
        this.commentCount = (this.commentCount == null ? 0 : this.commentCount) + 1;
    }

    /**
     * 减少评论数
     */
    public void decreaseCommentCount() {
        this.commentCount = Math.max(0, (this.commentCount == null ? 0 : this.commentCount) - 1);
    }
    
    /**
     * 增加分享数
     */
    public void increaseShareCount() {
        this.shareCount = (this.shareCount == null ? 0 : this.shareCount) + 1;
    }
    
    /**
     * 减少分享数
     */
    public void decreaseShareCount() {
        this.shareCount = Math.max(0, (this.shareCount == null ? 0 : this.shareCount) - 1);
    }

    /**
     * 验证是否可以发布
     */
    private void validateCanPublish() {
        if (PostStatus.PUBLISHED == this.status) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子已发布");
        }
        
        if (PostStatus.DELETED == this.status) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "已删除的帖子不能发布");
        }
        
        if (this.title == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子标题不能为空");
        }
        
        if (this.content == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子内容不能为空");
        }
        
        if (this.categoryId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子分类不能为空");
        }
    }

    /**
     * 验证用户是否有权限操作该帖子
     * @param currentUserId 当前用户ID
     * @param isAdmin 是否为管理员
     */
    public void validateOwnership(Long currentUserId, boolean isAdmin) {
        if (currentUserId == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户未登录");
        }
        
        // 如果是管理员，直接允许操作
        if (isAdmin) {
            return;
        }
        
        // 非管理员只能操作自己的帖子
        if (!this.userId.equals(currentUserId)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "无权操作此帖子");
        }
    }
    
    /**
     * 验证用户是否有权限操作该帖子（兼容旧方法）
     * @param currentUserId 当前用户ID
     */
    public void validateOwnership(Long currentUserId) {
        validateOwnership(currentUserId, false);
    }

    /**
     * 判断帖子是否已发布
     */
    public boolean isPublished() {
        return PostStatus.PUBLISHED == this.status;
    }

    /**
     * 判断帖子是否为草稿
     */
    public boolean isDraft() {
        return PostStatus.DRAFT == this.status;
    }

    /**
     * 判断帖子是否已删除
     */
    public boolean isDeleted() {
        return PostStatus.DELETED == this.status;
    }

    /**
     * 判断是否为问答帖
     */
    public boolean isQuestion() {
        return PostType.QUESTION.equals(this.type);
    }

    /**
     * 判断是否为讨论帖
     */
    public boolean isDiscussion() {
        return PostType.DISCUSSION.equals(this.type);
    }

    /**
     * 判断是否为文章
     */
    public boolean isArticle() {
        return PostType.POST.equals(this.type);
    }

    /**
     * 判断是否为普通帖子
     */
    public boolean isPost() {
        return PostType.POST.equals(this.type);
    }
    
    /**
     * 判断是否为资源分享帖
     */
    public boolean isResource() {
        return PostType.RESOURCE.equals(this.type);
    }

    /**
     * 获取标题值
     */
    public String getTitleValue() {
        return this.title != null ? this.title.getValue() : null;
    }

    /**
     * 获取内容值
     */
    public String getContentValue() {
        return this.content != null ? this.content.getValue() : null;
    }

    /**
     * 设置分类ID
     */
    public void setCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "分类ID不能为空");
        }
        this.categoryId = categoryId;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置帖子类型
     */
    public void setType(PostType type) {
        if (type == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子类型不能为空");
        }
        this.type = type;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置帖子状态
     */
    public void setStatus(PostStatus status) {
        if (status == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子状态不能为空");
        }
        this.status = status;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置是否加精
     */
    public void setFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断是否加精
     */
    public boolean isFeatured() {
        return this.isFeatured != null && this.isFeatured;
    }
    
    /**
     * 验证帖子创建前的完整性
     */
    public void validateForCreation() {
        if (this.title == null || this.title.getValue() == null || this.title.getValue().trim().isEmpty()) {
            throw new IllegalArgumentException("帖子标题不能为空");
        }
        
        if (this.content == null || this.content.getValue() == null || this.content.getValue().trim().isEmpty()) {
            throw new IllegalArgumentException("帖子内容不能为空");
        }
        
        if (this.userId == null) {
            throw new IllegalArgumentException("帖子作者ID不能为空");
        }
        
        if (this.categoryId == null) {
            throw new IllegalArgumentException("帖子分类ID不能为空");
        }
        
        // 验证标题和内容长度
        String titleValue = this.title.getValue();
        String contentValue = this.content.getValue();
        
        if (titleValue.isEmpty() || titleValue.length() > 100) {
            throw new IllegalArgumentException("帖子标题长度必须在1-100个字符之间");
        }
        
        if (contentValue.isEmpty() || contentValue.length() > 10000) {
            throw new BusinessException("帖子内容长度必须在1-10000个字符之间");
        }
        
        // 验证帖子类型
        if (this.type == null) {
            throw new IllegalArgumentException("帖子类型不能为空");
        }
    }
    
    /**
     * 获取状态编码
     */
    public int getStatusCode() {
        return this.status != null ? this.status.getCode() : PostStatus.DRAFT.getCode();
    }
    
    /**
     * 设置状态编码
     */
    public void setStatusCode(int statusCode) {
        this.status = PostStatus.fromCode(statusCode);
    }
}