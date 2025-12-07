package cn.xu.event.events;

import cn.xu.event.core.BaseEvent;
import lombok.Getter;

/**
 * 私信事件
 *
 * 
 */
@Getter
public class DMEvent extends BaseEvent {
    
    /** 接收者ID */
    private final Long receiverId;
    
    /** 消息ID */
    private final Long messageId;
    
    /** 消息内容预览 */
    private final String contentPreview;
    
    /** 是否为问候消息 */
    private final boolean greeting;
    
    /** 事件子类型 */
    private final DMEventType dmEventType;
    
    public enum DMEventType {
        /** 消息发送 */
        SENT,
        /** 消息已读 */
        READ
    }
    
    private DMEvent(Long senderId, Long receiverId, Long messageId, 
                   String contentPreview, boolean greeting, DMEventType eventType) {
        super(senderId, EventAction.CREATE);
        this.receiverId = receiverId;
        this.messageId = messageId;
        this.contentPreview = contentPreview;
        this.greeting = greeting;
        this.dmEventType = eventType;
    }
    
    /**
     * 获取发送者ID
     */
    public Long getSenderId() {
        return getOperatorId();
    }
    
    /**
     * 创建消息发送事件
     */
    public static DMEvent sent(Long senderId, Long receiverId, Long messageId, 
                               String contentPreview, boolean greeting) {
        String preview = contentPreview;
        if (preview != null && preview.length() > 50) {
            preview = preview.substring(0, 50) + "...";
        }
        return new DMEvent(senderId, receiverId, messageId, preview, greeting, DMEventType.SENT);
    }
    
    /**
     * 创建消息已读事件
     */
    public static DMEvent read(Long readerId, Long otherUserId) {
        return new DMEvent(readerId, otherUserId, null, null, false, DMEventType.READ);
    }
}
