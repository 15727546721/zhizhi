package cn.xu.domain.like.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞聚合根
 */
@Data
@Builder
public class Like {
    private Long id;
    private Long userId;
    private Long targetId;
    private LikeType type;
    private boolean liked;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 点赞
     */
    public void like() {
        this.liked = true;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 取消点赞
     */
    public void unlike() {
        this.liked = false;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 是否已点赞
     */
    public boolean isLiked() {
        return this.liked;
    }
} 