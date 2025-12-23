package cn.xu.event;

import cn.xu.model.entity.Notification;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 通知事件
 * 
 * <p>当有新通知产生时发布此事件，用于触发WebSocket推送</p>
 */
@Getter
public class NotificationEvent extends ApplicationEvent {

    /**
     * 通知实体
     */
    private final Notification notification;

    /**
     * 创建通知事件
     */
    public NotificationEvent(Object source, Notification notification) {
        super(source);
        this.notification = notification;
    }

    /**
     * 获取接收者ID
     */
    public Long getReceiverId() {
        return notification.getReceiverId();
    }

    /**
     * 获取通知类型
     */
    public Integer getType() {
        return notification.getType();
    }
}
