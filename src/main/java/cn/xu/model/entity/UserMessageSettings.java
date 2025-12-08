package cn.xu.model.entity;

import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户私信设置表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageSettings implements Serializable {
    
    /**
     * 设置ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 是否允许陌生人私信：0-不允许 1-允许
     */
    private Integer allowStrangerMessage;
    
    /**
     * 是否允许非互相关注用户私信：0-不允许 1-允许
     */
    private Integer allowNonMutualFollowMessage;
    
    /**
     * 是否开启私信通知：0-关闭 1-开启
     */
    private Integer messageNotificationEnabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // ==================== 业务方法 ====================
    
    /**
     * 创建默认设置
     * @param userId 用户ID
     * @param allowStrangerMessage 是否允许陌生人私信
     */
    public static UserMessageSettings createDefault(Long userId, Boolean allowStrangerMessage) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空或零");
        }
        LocalDateTime now = LocalDateTime.now();
        boolean allowStranger = allowStrangerMessage != null && allowStrangerMessage;
        return UserMessageSettings.builder()
                .userId(userId)
                .allowStrangerMessage(allowStranger ? 1 : 0)
                .allowNonMutualFollowMessage(allowStranger ? 1 : 0)
                .messageNotificationEnabled(1)
                .createTime(now)
                .updateTime(now)
                .build();
    }

    /**
     * 创建默认设置
     * @param userId 用户ID
     */
    public static UserMessageSettings createDefault(Long userId) {
        return createDefault(userId, true);
    }

    /**
     * 更新设置
     * @param allowStrangerMessage 是否允许陌生人私信
     * @param allowNonMutualFollowMessage 是否允许非互相关注用户私信
     * @param messageNotificationEnabled 是否开启私信通知
     */
    public void updateSettings(Boolean allowStrangerMessage, Boolean allowNonMutualFollowMessage, Boolean messageNotificationEnabled) {
        if (allowStrangerMessage != null) {
            this.allowStrangerMessage = allowStrangerMessage ? 1 : 0;
        }
        if (allowNonMutualFollowMessage != null) {
            this.allowNonMutualFollowMessage = allowNonMutualFollowMessage ? 1 : 0;
        }
        if (messageNotificationEnabled != null) {
            this.messageNotificationEnabled = messageNotificationEnabled ? 1 : 0;
        }
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 是否允许陌生人私信
     */
    public boolean isAllowStrangerMessage() {
        return allowStrangerMessage != null && allowStrangerMessage == 1;
    }

    /**
     * 是否允许非互相关注用户私信
     */
    public boolean isAllowNonMutualFollowMessage() {
        return allowNonMutualFollowMessage != null && allowNonMutualFollowMessage == 1;
    }

    /**
     * 是否开启私信通知
     */
    public boolean isMessageNotificationEnabled() {
        return messageNotificationEnabled != null && messageNotificationEnabled == 1;
    }
}