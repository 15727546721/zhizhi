package cn.xu.event.publisher;

import cn.xu.event.core.EventBus;
import cn.xu.event.events.PostEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 内容事件发布器
 * 
 * <p>负责发布内容类事件：
 * <ul>
 *   <li>帖子事件</li>
 * </ul>
 *
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventPublisher {
    
    private final EventBus eventBus;
    
    /**
     * 发布帖子创建事件
     */
    public void publishPostCreated(Long userId, Long postId, String title) {
        eventBus.publish(PostEvent.created(userId, postId, title));
    }
    
    /**
     * 发布帖子更新事件
     */
    public void publishPostUpdated(Long userId, Long postId, String title) {
        eventBus.publish(PostEvent.updated(userId, postId, title));
    }
    
    /**
     * 发布帖子删除事件
     */
    public void publishPostDeleted(Long userId, Long postId) {
        eventBus.publish(PostEvent.deleted(userId, postId));
    }
}
