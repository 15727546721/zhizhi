package cn.xu.domain.like.event;

import cn.xu.domain.like.model.LikeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 点赞事件发布器
 * 使用Spring Event机制发布点赞相关事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布点赞/取消点赞事件
     */
    public void publish(Long userId, Long targetId, LikeType targetType, Boolean likeStatus) {
        LikeEvent event = LikeEvent.builder()
                .userId(userId)
                .targetId(targetId)
                .type(targetType)
                .status(likeStatus)
                .createTime(LocalDateTime.now())
                .build();
                
        log.debug("发布点赞事件: userId={}, targetId={}, type={}, status={}", 
                 userId, targetId, targetType, likeStatus);
        eventPublisher.publishEvent(event);
    }
    
    /**
     * 发布点赞事件（带标题）
     */
    public void publish(Long userId, Long targetId, LikeType targetType, Boolean likeStatus, String targetTitle) {
        LikeEvent event = LikeEvent.builder()
                .userId(userId)
                .targetId(targetId)
                .type(targetType)
                .status(likeStatus)
                .createTime(LocalDateTime.now())
                .build();
                
        log.debug("发布点赞事件: userId={}, targetId={}, type={}, status={}, title={}", 
                 userId, targetId, targetType, likeStatus, targetTitle);
        eventPublisher.publishEvent(event);
    }
}