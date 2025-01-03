package cn.xu.domain.like.model;

import lombok.Getter;

/**
 * 点赞类型
 */
@Getter
public enum LikeType {
    
    ARTICLE(1, "文章", true),
    COMMENT(2, "评论", true),
    TOPIC(3, "话题", true),
    USER(4, "用户", false);  // 用户点赞暂不开放
    
    private final int code;
    private final String description;
    private final boolean enabled;
    
    LikeType(int code, String description, boolean enabled) {
        this.code = code;
        this.description = description;
        this.enabled = enabled;
    }
    
    /**
     * 检查点赞类型是否可用
     */
    public void checkEnabled() {
        if (!this.enabled) {
            throw new IllegalStateException(this.description + "点赞功能暂未开放");
        }
    }
    
    /**
     * 获取点赞数上限
     */
    public long getMaxLikeCount() {
        switch (this) {
            case ARTICLE:
                return 1_000_000L;  // 文章最多100万赞
            case COMMENT:
                return 100_000L;    // 评论最多10万赞
            case TOPIC:
                return 500_000L;    // 话题最多50万赞
            default:
                return 10_000L;     // 其他类型默认1万赞
        }
    }
    
    /**
     * 检查点赞数是否超出限制
     */
    public void checkLikeCount(long currentCount) {
        if (currentCount >= getMaxLikeCount()) {
            throw new IllegalStateException(this.description + "点赞数已达到上限");
        }
    }
    
    /**
     * 是否需要进行频率限制
     */
    public boolean needRateLimit() {
        return this != USER;  // 用户点赞不限制频率
    }
    
    /**
     * 获取缓存过期时间（天）
     */
    public int getCacheExpireDays() {
        switch (this) {
            case ARTICLE:
                return 30;  // 文章点赞数据保留30天
            case COMMENT:
                return 15;  // 评论点赞数据保留15天
            case TOPIC:
                return 7;   // 话题点赞数据保留7天
            default:
                return 3;   // 其他类型默认3天
        }
    }
    
    /**
     * 根据编码获取点赞类型
     */
    public static LikeType fromCode(int code) {
        for (LikeType type : LikeType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的点赞类型编码: " + code);
    }
} 