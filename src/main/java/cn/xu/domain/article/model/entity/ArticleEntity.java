package cn.xu.domain.article.model.entity;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.model.valobj.*;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章领域实体
 * 封装文章相关的业务逻辑和规则
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleEntity {
    private Long id;
    private Long categoryId;
    private ArticleTitle title;
    private String description;
    private ArticleContent content;
    private String coverUrl;
    private Long userId;
    private Long viewCount;
    private Long collectCount;
    private Long commentCount;
    private Long likeCount;
    private ArticleStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime publishTime; // 发布时间

    // ==================== 业务方法 ====================

    /**
     * 创建新文章草稿
     */
    public static ArticleEntity createDraft(Long userId, String title, String content, String description, Long categoryId) {
        return ArticleEntity.builder()
                .userId(userId)
                .title(new ArticleTitle(title))
                .content(new ArticleContent(content))
                .description(description)
                .categoryId(categoryId)
                .status(ArticleStatus.DRAFT)
                .viewCount(0L)
                .collectCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 发布文章
     */
    public void publish() {
        validateCanPublish();
        this.status = ArticleStatus.PUBLISHED;
        this.publishTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 撤回文章（回到草稿状态）
     */
    public void withdraw() {
        if (this.status != ArticleStatus.PUBLISHED) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "只有已发布的文章才能撤回");
        }
        this.status = ArticleStatus.DRAFT;
        this.publishTime = null;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 删除文章
     */
    public void delete() {
        if (this.status == ArticleStatus.DELETED) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "文章已被删除");
        }
        this.status = ArticleStatus.DELETED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新文章内容
     */
    public void updateContent(String title, String content, String description) {
        if (this.status == ArticleStatus.PUBLISHED) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "已发布的文章不能编辑，请先撤回到草稿状态");
        }
        
        this.title = new ArticleTitle(title);
        this.content = new ArticleContent(content);
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置文章封面
     */
    public void setCoverImage(String coverUrl) {
        if (coverUrl == null || coverUrl.trim().isEmpty()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "封面图片地址不能为空");
        }
        this.coverUrl = coverUrl;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 增加浏览量
     */
    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
        // 注意：浏览量的更新通常不需要更新updateTime，避免频繁的数据库写入
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
    public void increaseCollectCount() {
        this.collectCount = (this.collectCount == null ? 0 : this.collectCount) + 1;
    }

    /**
     * 减少收藏数
     */
    public void decreaseCollectCount() {
        this.collectCount = Math.max(0, (this.collectCount == null ? 0 : this.collectCount) - 1);
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
     * 验证是否可以发布
     */
    private void validateCanPublish() {
        if (this.status == ArticleStatus.PUBLISHED) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "文章已发布");
        }
        
        if (this.status == ArticleStatus.DELETED) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "已删除的文章不能发布");
        }
        
        if (this.title == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "文章标题不能为空");
        }
        
        if (this.content == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "文章内容不能为空");
        }
        
        if (this.categoryId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "文章分类不能为空");
        }
    }

    /**
     * 验证用户是否有权限操作该文章
     */
    public void validateOwnership(Long currentUserId) {
        if (currentUserId == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户未登录");
        }
        
        if (!this.userId.equals(currentUserId)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "无权操作此文章");
        }
    }

    /**
     * 判断文章是否已发布
     */
    public boolean isPublished() {
        return ArticleStatus.PUBLISHED.equals(this.status);
    }

    /**
     * 判断文章是否为草稿
     */
    public boolean isDraft() {
        return ArticleStatus.DRAFT.equals(this.status);
    }

    /**
     * 判断文章是否已删除
     */
    public boolean isDeleted() {
        return ArticleStatus.DELETED.equals(this.status);
    }

    /**
     * 计算文章热度分数
     */
    public double calculateHotScore() {
        // 基于浏览量、点赞数、评论数、收藏数和发布时间计算热度分数
        long views = this.viewCount != null ? this.viewCount : 0;
        long likes = this.likeCount != null ? this.likeCount : 0;
        long comments = this.commentCount != null ? this.commentCount : 0;
        long collects = this.collectCount != null ? this.collectCount : 0;
        
        // 时间衰减因子（发布时间越久，热度越低）
        double timeDecay = calculateTimeDecay();
        
        // 热度计算公式
        return (views * 1 + likes * 3 + comments * 2 + collects * 4) * timeDecay;
    }

    /**
     * 计算时间衰减因子
     */
    private double calculateTimeDecay() {
        if (publishTime == null) return 1.0;
        
        long hoursSincePublish = java.time.Duration.between(publishTime, LocalDateTime.now()).toHours();
        // 使用指数衰减，每24小时热度衰减一半
        return Math.pow(0.5, hoursSincePublish / 24.0);
    }

    // ==================== 兼容性方法 ====================
    
    /**
     * 获取标题字符串（兼容现有代码）
     */
    public String getTitleValue() {
        return title != null ? title.getValue() : null;
    }
    
    /**
     * 获取内容字符串（兼容现有代码）
     */
    public String getContentValue() {
        return content != null ? content.getValue() : null;
    }
    
    /**
     * 设置标题（兼容现有代码）
     */
    public void setTitleValue(String titleValue) {
        this.title = titleValue != null ? new ArticleTitle(titleValue) : null;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 设置内容（兼容现有代码）
     */
    public void setContentValue(String contentValue) {
        this.content = contentValue != null ? new ArticleContent(contentValue) : null;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 验证文章创建前的完整性
     */
    public void validateForCreation() {
        if (this.title == null) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
        
        if (this.content == null) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        
        if (this.userId == null) {
            throw new IllegalArgumentException("文章作者ID不能为空");
        }
        
        if (this.categoryId == null) {
            throw new IllegalArgumentException("文章分类ID不能为空");
        }
    }
}
