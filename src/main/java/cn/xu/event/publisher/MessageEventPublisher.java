package cn.xu.event.publisher;

import cn.xu.event.core.EventBus;
import cn.xu.event.events.DMEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 消息事件发布器
 * 
 * <p>负责发布私信类事件
 *
 * 
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventPublisher {
    
    private final EventBus eventBus;
    
    /**
     * 发布私信发送事件
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param messageId 消息ID
     * @param contentPreview 内容预览
     * @param isGreeting 是否为问候消息
     */
    public void publishSent(Long senderId, Long receiverId, Long messageId, 
                            String contentPreview, boolean isGreeting) {
        eventBus.publish(DMEvent.sent(senderId, receiverId, messageId, contentPreview, isGreeting));
    }
    
    /**
     * 发布私信已读事件
     * 
     * @param readerId 阅读者ID
     * @param otherUserId 对方用户ID
     */
    public void publishRead(Long readerId, Long otherUserId) {
        eventBus.publish(DMEvent.read(readerId, otherUserId));
    }
    
    /**
     * 发布私信撤回事件
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param messageId 消息ID
     */
    public void publishWithdrawn(Long senderId, Long receiverId, Long messageId) {
        eventBus.publish(DMEvent.withdrawn(senderId, receiverId, messageId));
    }
}
