package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 帖子统计表PO
 * 用于存储帖子的各项统计数据，避免频繁跨域查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 浏览量
     */
    private Long viewCount;
    
    /**
     * 评论数
     */
    private Long commentCount;
    
    /**
     * 点赞数
     */
    private Long likeCount;
    
    /**
     * 收藏数
     */
    private Long favoriteCount;
    
    /**
     * 分享数
     */
    private Long shareCount;
    
    /**
     * 热度分数
     */
    private BigDecimal hotScore;
    
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
     * 增加浏览量
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
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
     * 增加点赞数
     */
    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }
    
    /**
     * 减少点赞数
     */
    public void decrementLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) - 1;
        if (this.likeCount < 0) {
            this.likeCount = 0L;
        }
    }
    
    /**
     * 增加收藏数
     */
    public void incrementFavoriteCount() {
        this.favoriteCount = (this.favoriteCount == null ? 0 : this.favoriteCount) + 1;
    }
    
    /**
     * 减少收藏数
     */
    public void decrementFavoriteCount() {
        this.favoriteCount = (this.favoriteCount == null ? 0 : this.favoriteCount) - 1;
        if (this.favoriteCount < 0) {
            this.favoriteCount = 0L;
        }
    }
    
    /**
     * 创建新的统计记录
     */
    public static PostStatistics createNew(Long postId) {
        return PostStatistics.builder()
                .postId(postId)
                .viewCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .favoriteCount(0L)
                .shareCount(0L)
                .hotScore(BigDecimal.ZERO)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
}
