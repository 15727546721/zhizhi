package cn.xu.domain.message.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigEntity {
    /**
     * 配置ID
     */
    private Long id;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置描述
     */
    private String configDesc;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 获取配置值（转换为整数）
     */
    public Integer getIntValue() {
        try {
            return Integer.parseInt(configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 获取配置值（转换为布尔值）
     */
    public Boolean getBooleanValue() {
        if (configValue == null) {
            return null;
        }
        return "1".equals(configValue) || "true".equalsIgnoreCase(configValue);
    }
    
    /**
     * 获取配置值（转换为长整数）
     */
    public Long getLongValue() {
        try {
            return Long.parseLong(configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 更新配置值
     */
    public void updateValue(String value) {
        this.configValue = value;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 创建系统配置
     */
    public static SystemConfigEntity create(String configKey, String configValue, String configDesc) {
        if (configKey == null || configKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Config key cannot be null or empty");
        }
        if (configValue == null) {
            throw new IllegalArgumentException("Config value cannot be null");
        }
        LocalDateTime now = LocalDateTime.now();
        return SystemConfigEntity.builder()
                .configKey(configKey)
                .configValue(configValue)
                .configDesc(configDesc)
                .createTime(now)
                .updateTime(now)
                .build();
    }
}

