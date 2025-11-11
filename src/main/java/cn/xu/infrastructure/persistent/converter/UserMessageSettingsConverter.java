package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.message.model.entity.UserMessageSettingsEntity;
import cn.xu.infrastructure.persistent.po.UserMessageSettings;
import org.springframework.stereotype.Component;

/**
 * 用户私信设置转换器
 * 负责领域实体与持久化对象之间的转换
 */
@Component
public class UserMessageSettingsConverter {
    
    /**
     * 领域实体转持久化对象
     */
    public UserMessageSettings toDataObject(UserMessageSettingsEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserMessageSettings.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .allowStrangerMessage(entity.getAllowStrangerMessage() != null && entity.getAllowStrangerMessage() ? 1 : 0)
                .allowNonMutualFollowMessage(entity.getAllowNonMutualFollowMessage() != null && entity.getAllowNonMutualFollowMessage() ? 1 : 0)
                .messageNotificationEnabled(entity.getMessageNotificationEnabled() != null && entity.getMessageNotificationEnabled() ? 1 : 0)
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
    
    /**
     * 持久化对象转领域实体
     */
    public UserMessageSettingsEntity toDomainEntity(UserMessageSettings po) {
        if (po == null) {
            return null;
        }
        return UserMessageSettingsEntity.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .allowStrangerMessage(po.getAllowStrangerMessage() != null && po.getAllowStrangerMessage() == 1)
                .allowNonMutualFollowMessage(po.getAllowNonMutualFollowMessage() != null && po.getAllowNonMutualFollowMessage() == 1)
                .messageNotificationEnabled(po.getMessageNotificationEnabled() != null && po.getMessageNotificationEnabled() == 1)
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}

