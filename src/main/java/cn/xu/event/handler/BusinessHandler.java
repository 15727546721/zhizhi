package cn.xu.event.handler;

import cn.xu.event.events.FollowEvent;
import cn.xu.event.events.UserEvent;
import cn.xu.repository.IGreetingRecordRepository;
import cn.xu.repository.IUserConversationRepository;
import cn.xu.service.follow.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 业务副作用处理器
 * 
 * <p>处理各种业务事件产生的副作用：
 * <ul>
 *   <li>关注时清理问候记录</li>
 *   <li>用户资料更新时同步会话信息</li>
 * </ul>
 *
 * 
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessHandler {
    
    private final FollowService followService;
    private final IGreetingRecordRepository greetingRecordRepository;
    private final IUserConversationRepository conversationRepository;
    
    // ==================== 关注副作用 ====================
    
    /**
     * 处理关注事件 - 关注时清理问候记录
     */
    @Async
    @EventListener
    public void onFollow(FollowEvent event) {
        if (!event.isFollowed()) return;
        
        try {
            Long followerId = event.getFollowerId();
            Long followeeId = event.getFolloweeId();
            
            // 检查是否互相关注
            boolean isMutual = followService.isFollowed(followeeId, followerId);
            
            if (isMutual) {
                // 互相关注了，清理双方的问候记录
                greetingRecordRepository.deleteBidirectionalGreeting(followerId, followeeId);
                log.info("[Handler] 互相关注成功，已清理问候记录 - user1:{}, user2:{}", followerId, followeeId);
            }
        } catch (Exception e) {
            log.error("[Handler] 处理关注副作用失败", e);
        }
    }
    
    // ==================== 用户资料副作用 ====================
    
    /**
     * 处理用户资料更新事件 - 同步会话中的用户信息
     */
    @Async
    @EventListener
    public void onUserUpdated(UserEvent event) {
        if (event.getUserEventType() != UserEvent.UserEventType.UPDATED) return;
        
        try {
            Long userId = event.getUserId();
            String nickname = event.getNickname();
            String avatar = event.getAvatar();
            
            if (nickname != null || avatar != null) {
                conversationRepository.syncUserInfo(userId, nickname, avatar);
                log.info("[Handler] 已同步用户信息到会话 - userId:{}", userId);
            }
        } catch (Exception e) {
            log.error("[Handler] 处理用户资料更新副作用失败", e);
        }
    }
}
