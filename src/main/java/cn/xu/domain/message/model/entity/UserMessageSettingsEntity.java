package cn.xu.domain.message.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户私信设置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageSettingsEntity {
    /**
     * 设置ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 是否允许陌生人私信：false-不允许 true-允许
     */
    private Boolean allowStrangerMessage;
    
    /**
     * 是否允许非互相关注用户私信：false-不允许 true-允许
     */
    private Boolean allowNonMutualFollowMessage;
    
    /**
     * 是否开启私信通知：false-关闭 true-开启
     */
    private Boolean messageNotificationEnabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建默认设置
     * @param userId 用户ID
     * @param allowStrangerMessage 是否允许陌生人私信（从系统配置获取）
     */
    public static UserMessageSettingsEntity createDefault(Long userId, Boolean allowStrangerMessage) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or zero");
        }
        LocalDateTime now = LocalDateTime.now();
        // 如果系统允许陌生人私信，则同时允许非互相关注用户私信
        boolean allowStranger = allowStrangerMessage != null && allowStrangerMessage;
        return UserMessageSettingsEntity.builder()
                .userId(userId)
                .allowStrangerMessage(allowStranger)
                .allowNonMutualFollowMessage(allowStranger)
                .messageNotificationEnabled(true)
                .createTime(now)
                .updateTime(now)
                .build();
    }
    
    /**
     * 创建默认设置（使用系统默认值：允许所有人）
     */
    public static UserMessageSettingsEntity createDefault(Long userId) {
        return createDefault(userId, true);
    }
    
    /**
     * 更新设置
     */
    public void updateSettings(Boolean allowStrangerMessage, Boolean allowNonMutualFollowMessage, Boolean messageNotificationEnabled) {
        if (allowStrangerMessage != null) {
            this.allowStrangerMessage = allowStrangerMessage;
        }
        if (allowNonMutualFollowMessage != null) {
            this.allowNonMutualFollowMessage = allowNonMutualFollowMessage;
        }
        if (messageNotificationEnabled != null) {
            this.messageNotificationEnabled = messageNotificationEnabled;
        }
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 检查是否允许陌生人私信
     */
    public boolean isAllowStrangerMessage() {
        return allowStrangerMessage != null && allowStrangerMessage;
    }
    
    /**
     * 检查是否允许非互相关注用户私信
     */
    public boolean isAllowNonMutualFollowMessage() {
        return allowNonMutualFollowMessage != null && allowNonMutualFollowMessage;
    }
    
    /**
     * 检查是否开启私信通知
     */
    public boolean isMessageNotificationEnabled() {
        return messageNotificationEnabled != null && messageNotificationEnabled;
    }
}

