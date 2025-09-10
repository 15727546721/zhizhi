package cn.xu.domain.follow.event;

import cn.xu.domain.follow.model.aggregate.FollowAggregate;
import cn.xu.domain.follow.service.FollowCacheDomainService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 关注事件处理器
 * 处理关注相关的领域事件
 */
@Component
@RequiredArgsConstructor
public class FollowEventHandler {
    
    private static final Logger log = LoggerFactory.getLogger(FollowEventHandler.class);
    
    private final FollowCacheDomainService followCacheDomainService;

    /**
     * 处理关注关系创建事件
     */
    @EventListener
    @Async
    public void handleFollowCreated(FollowAggregate.FollowCreatedEvent event) {
        try {
            log.info("[关注事件] 处理关注关系创建事件 - ID: {}, 关注者: {}, 被关注者: {}", 
                    event.getFollowId(), event.getFollowerId(), event.getFollowedId());
            
            // 更新相关缓存
            followCacheDomainService.removeFollowRelationCache(event.getFollowerId(), event.getFollowedId());
            
            log.info("[关注事件] 关注关系创建事件处理完成");
        } catch (Exception e) {
            log.error("[关注事件] 处理关注关系创建事件失败 - ID: {}, 关注者: {}, 被关注者: {}", 
                     event.getFollowId(), event.getFollowerId(), event.getFollowedId(), e);
        }
    }

    /**
     * 处理关注事件
     */
    @EventListener
    @Async
    public void handleFollowed(FollowAggregate.FollowedEvent event) {
        try {
            log.info("[关注事件] 处理关注事件 - ID: {}, 关注者: {}, 被关注者: {}", 
                    event.getFollowId(), event.getFollowerId(), event.getFollowedId());
            
            // 更新相关缓存
            followCacheDomainService.removeFollowRelationCache(event.getFollowerId(), event.getFollowedId());
            followCacheDomainService.removeUserFollowCache(event.getFollowerId());
            followCacheDomainService.removeUserFollowCache(event.getFollowedId());
            
            log.info("[关注事件] 关注事件处理完成");
        } catch (Exception e) {
            log.error("[关注事件] 处理关注事件失败 - ID: {}, 关注者: {}, 被关注者: {}", 
                     event.getFollowId(), event.getFollowerId(), event.getFollowedId(), e);
        }
    }

    /**
     * 处理取消关注事件
     */
    @EventListener
    @Async
    public void handleUnfollowed(FollowAggregate.UnfollowedEvent event) {
        try {
            log.info("[关注事件] 处理取消关注事件 - ID: {}, 关注者: {}, 被关注者: {}", 
                    event.getFollowId(), event.getFollowerId(), event.getFollowedId());
            
            // 更新相关缓存
            followCacheDomainService.removeFollowRelationCache(event.getFollowerId(), event.getFollowedId());
            followCacheDomainService.removeUserFollowCache(event.getFollowerId());
            followCacheDomainService.removeUserFollowCache(event.getFollowedId());
            
            log.info("[关注事件] 取消关注事件处理完成");
        } catch (Exception e) {
            log.error("[关注事件] 处理取消关注事件失败 - ID: {}, 关注者: {}, 被关注者: {}", 
                     event.getFollowId(), event.getFollowerId(), event.getFollowedId(), e);
        }
    }
}