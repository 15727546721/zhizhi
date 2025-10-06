package cn.xu.domain.post.event;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.infrastructure.event.disruptor.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 帖子事件发布服务（应用服务）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostEventPublisher {

    private final EventPublisher eventPublisher;

    /**
     * 发布帖子创建事件
     */
    public void publishCreated(PostEntity post) {
        PostEvent event = PostEvent.builder()
                .post(post)
                .eventType(PostEvent.PostEventType.CREATED)
                .createTime(LocalDateTime.now())
                .operatorId(post.getUserId())
                .build();
        log.info("[帖子服务] 发布帖子创建事件: {}", event);
        eventPublisher.publishEvent(event, "PostEvent");
        
        // 发布新的帖子创建事件
        PostCreatedEvent createdEvent = PostCreatedEvent.builder()
                .postId(post.getId())
                .userId(post.getUserId())
                .title(post.getTitle().getTitle())
                .createTime(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(createdEvent, "PostCreatedEvent");
    }

    /**
     * 发布帖子更新事件
     */
    public void publishUpdated(PostEntity post) {
        PostEvent event = PostEvent.builder()
                .post(post)
                .eventType(PostEvent.PostEventType.UPDATED)
                .createTime(LocalDateTime.now())
                .operatorId(post.getUserId())
                .build();
        log.info("[帖子服务] 发布帖子更新事件: {}", event);
        eventPublisher.publishEvent(event, "PostEvent");
        
        // 发布新的帖子更新事件
        PostUpdatedEvent updatedEvent = PostUpdatedEvent.builder()
                .postId(post.getId())
                .userId(post.getUserId())
                .title(post.getTitle().getTitle())
                .updateTime(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(updatedEvent, "PostUpdatedEvent");
    }

    /**
     * 发布帖子删除事件
     */
    public void publishDeleted(Long postId) {
        PostEntity post = PostEntity.builder()
                .id(postId)
                .build();
        PostEvent event = PostEvent.builder()
                .post(post)
                .eventType(PostEvent.PostEventType.DELETED)
                .createTime(LocalDateTime.now())
                .build();
        log.info("[帖子服务] 发布帖子删除事件: {}", event);
        eventPublisher.publishEvent(event, "PostEvent");
        
        // 发布新的帖子删除事件
        PostDeletedEvent deletedEvent = PostDeletedEvent.builder()
                .postId(postId)
                .deleteTime(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(deletedEvent, "PostDeletedEvent");
    }
}