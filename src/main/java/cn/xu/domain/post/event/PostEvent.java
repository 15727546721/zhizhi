package cn.xu.domain.post.event;

import cn.xu.domain.post.model.entity.PostEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 帖子事件基类
 * 用于在领域内传递帖子相关的事件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostEvent {

    /**
     * 帖子实体
     */
    private PostEntity post;

    /**
     * 事件类型
     */
    private PostEventType eventType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 操作用户ID
     */
    private Long operatorId;

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