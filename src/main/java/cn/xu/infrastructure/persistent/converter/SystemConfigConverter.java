package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.message.model.entity.SystemConfigEntity;
import cn.xu.infrastructure.persistent.po.SystemConfig;
import org.springframework.stereotype.Component;

/**
 * 系统配置转换器
 * 负责领域实体与持久化对象之间的转换
 */
@Component
public class SystemConfigConverter {
    
    /**
     * 领域实体转持久化对象
     */
    public SystemConfig toDataObject(SystemConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        return SystemConfig.builder()
                .id(entity.getId())
                .configKey(entity.getConfigKey())
                .configValue(entity.getConfigValue())
                .configDesc(entity.getConfigDesc())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
    
    /**
     * 持久化对象转领域实体
     */
    public SystemConfigEntity toDomainEntity(SystemConfig po) {
        if (po == null) {
            return null;
        }
        return SystemConfigEntity.builder()
                .id(po.getId())
                .configKey(po.getConfigKey())
                .configValue(po.getConfigValue())
                .configDesc(po.getConfigDesc())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}

