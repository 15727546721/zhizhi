package cn.xu.model.dto;

import lombok.Data;

/**
 * 更新用户私信设置DTO
 */
@Data
public class UpdateUserMessageSettingsDTO {
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

