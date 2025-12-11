package cn.xu.event.events;

import cn.xu.event.core.BaseEvent;
import lombok.Getter;

/**
 * 用户事件
 *
 *
 */
@Getter
public class UserEvent extends BaseEvent {
    
    /** 事件子类型 */
    private final UserEventType userEventType;
    
    /** 用户昵称 */
    private final String nickname;
    
    /** 用户头像 */
    private final String avatar;
    
    public enum UserEventType {
        /** 注册 */
        REGISTERED,
        /** 登录 */
        LOGGED_IN,
        /** 资料更新 */
        UPDATED
    }
    
    private UserEvent(Long userId, UserEventType eventType, String nickname, String avatar) {
        super(userId, eventType == UserEventType.REGISTERED ? EventAction.CREATE : EventAction.UPDATE);
        this.userEventType = eventType;
        this.nickname = nickname;
        this.avatar = avatar;
    }
    
    public Long getUserId() {
        return getOperatorId();
    }
    
    public static UserEvent registered(Long userId, String nickname) {
        return new UserEvent(userId, UserEventType.REGISTERED, nickname, null);
    }
    
    public static UserEvent loggedIn(Long userId) {
        return new UserEvent(userId, UserEventType.LOGGED_IN, null, null);
    }
    
    public static UserEvent updated(Long userId, String nickname, String avatar) {
        return new UserEvent(userId, UserEventType.UPDATED, nickname, avatar);
    }
}
