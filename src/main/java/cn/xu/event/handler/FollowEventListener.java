package cn.xu.event.handler;

import cn.xu.event.events.FollowEvent;
import cn.xu.model.entity.PrivateMessage;
import cn.xu.model.entity.UserConversation;
import cn.xu.repository.PrivateMessageRepository;
import cn.xu.repository.UserConversationRepository;
import cn.xu.service.follow.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 关注事件监听器
 * 
 * <p>处理关注相关的副作用：
 * <ul>
 *   <li>互关时更新私信消息状态（PENDING → DELIVERED）</li>
 *   <li>互关时更新会话关系类型</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FollowEventListener {

    private final FollowService followService;
    private final PrivateMessageRepository messageRepository;
    private final UserConversationRepository conversationRepository;

    /**
     * 处理关注事件
     * 
     * <p>当用户A关注用户B时，检查是否形成互关：
     * <ul>
     *   <li>如果B也关注了A（互关），更新双方的打招呼消息状态为已送达</li>
     *   <li>同时更新双方会话的关系类型为互关</li>
     * </ul>
     */
    @Async
    @EventListener
    public void handleFollowEvent(FollowEvent event) {
        if (!event.isFollowed()) {
            // 取消关注不需要处理
            return;
        }

        Long followerId = event.getFollowerId();
        Long followeeId = event.getFolloweeId();

        log.info("[关注事件] 收到关注事件 - followerId: {}, followeeId: {}", followerId, followeeId);

        try {
            // 检查是否形成互关
            boolean isMutual = followService.isFollowed(followeeId, followerId);
            
            if (isMutual) {
                log.info("[关注事件] 检测到互关 - {} <-> {}", followerId, followeeId);
                handleMutualFollow(followerId, followeeId);
            }
        } catch (Exception e) {
            log.error("[关注事件] 处理失败 - followerId: {}, followeeId: {}", followerId, followeeId, e);
        }
    }

    /**
     * 处理互关逻辑
     */
    private void handleMutualFollow(Long userId1, Long userId2) {
        // 1. 更新双方的打招呼消息状态为已送达
        updatePendingMessages(userId1, userId2);
        updatePendingMessages(userId2, userId1);

        // 2. 更新双方会话的关系类型为互关
        updateConversationRelationType(userId1, userId2);
        updateConversationRelationType(userId2, userId1);

        // 3. 清理打招呼记录（如果有的话）
        // 这里不需要处理，因为消息状态已经更新，权限检查会通过互关判断
        
        log.info("[关注事件] 互关处理完成 - {} <-> {}", userId1, userId2);
    }

    /**
     * 更新打招呼消息状态
     */
    private void updatePendingMessages(Long senderId, Long receiverId) {
        try {
            messageRepository.updatePendingToDelivered(senderId, receiverId);
            log.debug("[关注事件] 更新消息状态 - sender: {} -> receiver: {}", senderId, receiverId);
        } catch (Exception e) {
            log.warn("[关注事件] 更新消息状态失败 - sender: {}, receiver: {}", senderId, receiverId, e);
        }
    }

    /**
     * 更新会话关系类型为互关
     */
    private void updateConversationRelationType(Long ownerId, Long otherId) {
        try {
            conversationRepository.findByOwnerAndOther(ownerId, otherId)
                    .ifPresent(conv -> {
                        if (conv.getRelationType() != UserConversation.RELATION_MUTUAL) {
                            conv.setRelationType(UserConversation.RELATION_MUTUAL);
                            conversationRepository.update(conv);
                            log.debug("[关注事件] 更新会话关系类型为互关 - owner: {}, other: {}", ownerId, otherId);
                        }
                    });
        } catch (Exception e) {
            log.warn("[关注事件] 更新会话关系类型失败 - owner: {}, other: {}", ownerId, otherId, e);
        }
    }
}
