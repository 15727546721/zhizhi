package cn.xu.domain.post.event;

import cn.xu.domain.post.model.entity.PostEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 帖子事件发布器
 * 使用Spring Event机制发布帖子相关事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布帖子创建事件
     */
    public void publishCreated(PostEntity post) {
        PostCreatedEvent event = PostCreatedEvent.builder()
                .postId(post.getId())
                .userId(post.getUserId())
                .title(post.getTitle() != null ? post.getTitle().getValue() : null)
                .description(post.getDescription())
                .createTime(LocalDateTime.now())
                .build();
        log.debug("发布帖子创建事件: postId={}", post.getId());
        eventPublisher.publishEvent(event);
    }

    /**
     * 发布帖子更新事件
     */
    public void publishUpdated(PostEntity post) {
        PostUpdatedEvent event = PostUpdatedEvent.builder()
                .postId(post.getId())
                .userId(post.getUserId())
                .title(post.getTitle() != null ? post.getTitle().getValue() : null)
                .description(post.getDescription())
                .updateTime(LocalDateTime.now())
                .build();
        log.debug("发布帖子更新事件: postId={}", post.getId());
        eventPublisher.publishEvent(event);
    }

    /**
     * 发布帖子删除事件
     */
    public void publishDeleted(Long postId) {
        PostDeletedEvent event = PostDeletedEvent.builder()
                .postId(postId)
                .deleteTime(LocalDateTime.now())
                .build();
        log.debug("发布帖子删除事件: postId={}", postId);
        eventPublisher.publishEvent(event);
    }
}