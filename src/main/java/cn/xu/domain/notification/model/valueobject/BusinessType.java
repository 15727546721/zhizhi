package cn.xu.domain.notification.model.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 业务类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum BusinessType {
    /**
     * 文章
     */
    ARTICLE("文章"),
    
    /**
     * 评论
     */
    COMMENT("评论"),
    
    /**
     * 回复
     */
    REPLY("回复"),
    
    /**
     * 话题
     */
    TOPIC("话题"),
    
    /**
     * 用户
     */
    USER("用户");
    
    private final String description;

    /**
     * 检查业务类型是否支持点赞操作
     */
    public boolean isSupportLike() {
        return this == ARTICLE || this == COMMENT || this == REPLY || this == TOPIC;
    }

    /**
     * 检查业务类型是否支持评论操作
     */
    public boolean isSupportComment() {
        return this == ARTICLE || this == TOPIC;
    }

    /**
     * 检查业务类型是否支持回复操作
     */
    public boolean isSupportReply() {
        return this == COMMENT;
    }

    /**
     * 检查业务类型是否支持收藏操作
     */
    public boolean isSupportFavorite() {
        return this == ARTICLE || this == TOPIC;
    }

    /**
     * 获取业务类型的中文描述
     */
    public String getActionDescription(NotificationType type) {
        if (type == null) {
            throw new IllegalArgumentException("通知类型不能为空");
        }
        
        switch (type) {
            case LIKE:
                return "赞了你的" + this.description;
            case COMMENT:
                return "评论了你的" + this.description;
            case REPLY:
                return "回复了你的" + this.description;
            case FAVORITE:
                return "收藏了你的" + this.description;
            case FOLLOW:
                return "关注了你";
            case SYSTEM:
                return "系统通知";
            default:
                throw new IllegalArgumentException("不支持的通知类型: " + type);
        }
    }
} 