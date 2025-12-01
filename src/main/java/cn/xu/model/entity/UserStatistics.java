package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户统计表PO
 * 用于存储用户的各项统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 发帖数
     */
    private Long postCount;
    
    /**
     * 评论数
     */
    private Long commentCount;
    
    /**
     * 文章数
     */
    private Long essayCount;
    
    /**
     * 关注数
     */
    private Long followCount;
    
    /**
     * 粉丝数
     */
    private Long fansCount;
    
    /**
     * 获赞数
     */
    private Long likeCount;
    
    /**
     * 被收藏数
     */
    private Long favoriteCount;
    
    /**
     * 被浏览数
     */
    private Long viewCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // ========== 业务方法 ==========
    
    /**
     * 增加发帖数
     */
    public void incrementPostCount() {
        this.postCount = (this.postCount == null ? 0 : this.postCount) + 1;
    }
    
    /**
     * 减少发帖数
     */
    public void decrementPostCount() {
        this.postCount = (this.postCount == null ? 0 : this.postCount) - 1;
        if (this.postCount < 0) {
            this.postCount = 0L;
        }
    }
    
    /**
     * 增加评论数
     */
    public void incrementCommentCount() {
        this.commentCount = (this.commentCount == null ? 0 : this.commentCount) + 1;
    }
    
    /**
     * 减少评论数
     */
    public void decrementCommentCount() {
        this.commentCount = (this.commentCount == null ? 0 : this.commentCount) - 1;
        if (this.commentCount < 0) {
            this.commentCount = 0L;
        }
    }
    
    /**
     * 增加关注数
     */
    public void incrementFollowCount() {
        this.followCount = (this.followCount == null ? 0 : this.followCount) + 1;
    }
    
    /**
     * 减少关注数
     */
    public void decrementFollowCount() {
        this.followCount = (this.followCount == null ? 0 : this.followCount) - 1;
        if (this.followCount < 0) {
            this.followCount = 0L;
        }
    }
    
    /**
     * 增加粉丝数
     */
    public void incrementFansCount() {
        this.fansCount = (this.fansCount == null ? 0 : this.fansCount) + 1;
    }
    
    /**
     * 减少粉丝数
     */
    public void decrementFansCount() {
        this.fansCount = (this.fansCount == null ? 0 : this.fansCount) - 1;
        if (this.fansCount < 0) {
            this.fansCount = 0L;
        }
    }
    
    /**
     * 创建新的统计记录
     */
    public static UserStatistics createNew(Long userId) {
        return UserStatistics.builder()
                .userId(userId)
                .postCount(0L)
                .commentCount(0L)
                .essayCount(0L)
                .followCount(0L)
                .fansCount(0L)
                .likeCount(0L)
                .favoriteCount(0L)
                .viewCount(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
}
