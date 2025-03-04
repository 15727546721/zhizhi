package cn.xu.domain.like.model;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 点赞聚合根
 */
@Getter
@ToString
public class Like {

    private Long id;
    private Long userId;
    private Long targetId;
    private LikeType type;
    private boolean liked;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Like(Long userId, Long targetId, LikeType type) {
        this.userId = userId;
        this.targetId = targetId;
        this.type = type;
        this.liked = true;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 创建点赞
     */
    public static Like create(Long userId, Long targetId, LikeType type) {
        return new Like(userId, targetId, type);
    }

    /**
     * 取消点赞
     */
    public void cancel() {
        this.liked = false;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 恢复点赞
     */
    public void restore() {
        this.liked = true;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置ID（仅供基础设施层使用）
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 是否是有效的点赞记录
     */
    public boolean isValid() {
        return userId != null && targetId != null && type != null;
    }
} 