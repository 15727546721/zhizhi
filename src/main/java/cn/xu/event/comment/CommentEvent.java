package cn.xu.event.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统一的评论事件
 * 使用action枚举区分不同操作类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEvent {
    /**
     * 事件操作类型
     */
    private CommentAction action;
    
    /**
     * 评论ID
     */
    private Long commentId;
    
    /**
     * 评论用户ID
     */
    private Long userId;
    
    /**
     * 评论目标类型（1=帖子 2=随笔等）
     */
    private Integer targetType;
    
    /**
     * 目标ID（文章ID/随笔ID等）
     */
    private Long targetId;
    
    /**
     * 父评论ID（一级评论为null）
     */
    private Long parentId;
    
    /**
     * 被回复用户ID
     */
    private Long replyUserId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 评论图片URL列表
     */
    private List<String> imageUrls;
    
    /**
     * 是否一级评论
     */
    private Boolean isRootComment;
    
    /**
     * 点赞操作标识（仅LIKED时使用）
     */
    private Boolean isLike;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime occurredTime;
    
    /**
     * 评论操作类型枚举
     */
    public enum CommentAction {
        /**
         * 创建评论
         */
        CREATED,
        
        /**
         * 更新评论
         */
        UPDATED,
        
        /**
         * 删除评论
         */
        DELETED,
        
        /**
         * 点赞评论
         */
        LIKED
    }
}