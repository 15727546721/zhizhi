package cn.xu.api.web.model.vo;

import lombok.Data;

/**
 * 用户私信设置VO
 */
@Data
public class UserMessageSettingsVO {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 是否允许陌生人私信
     */
    private Boolean allowStrangerMessage;
    
    /**
     * 是否允许非互相关注用户私信
     */
    private Boolean allowNonMutualFollowMessage;
    
    /**
     * 是否开启私信通知
     */
    private Boolean messageNotificationEnabled;
}

