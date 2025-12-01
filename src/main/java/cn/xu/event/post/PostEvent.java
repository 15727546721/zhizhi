package cn.xu.event.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一的帖子事件
 * 使用eventType枚举区分不同操作类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostEvent {

    /**
     * 事件类型
     */
    private PostEventType eventType;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子描述
     */
    private String description;

    /**
     * 事件发生时间
     */
    private LocalDateTime occurredTime;

    public enum PostEventType {
        /**
         * 帖子创建事件
         */
        CREATED,

        /**
         * 帖子更新事件
         */
        UPDATED,

        /**
         * 帖子删除事件
         */
        DELETED
    }
}