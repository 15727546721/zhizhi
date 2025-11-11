package cn.xu.domain.message.service;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.message.model.entity.SystemConfigEntity;
import cn.xu.domain.message.repository.ISystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 系统配置领域服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigDomainService {
    
    private final ISystemConfigRepository configRepository;
    
    /**
     * 允许通过API修改的配置键白名单
     */
    private static final Set<String> ALLOWED_CONFIG_KEYS = new HashSet<>(Arrays.asList(
        "private_message.enabled",
        "private_message.allow_stranger",
        "private_message.max_message_length",
        "private_message.rate_limit"
    ));
    
    /**
     * 获取配置值（字符串）
     */
    public String getConfigValue(String configKey) {
        Optional<SystemConfigEntity> configOpt = configRepository.findByKey(configKey);
        if (configOpt.isPresent()) {
            return configOpt.get().getConfigValue();
        }
        log.warn("[系统配置领域服务] 配置不存在 - 配置键: {}", configKey);
        return null;
    }
    
    /**
     * 获取配置值（整数）
     */
    public Integer getConfigIntValue(String configKey) {
        Optional<SystemConfigEntity> configOpt = configRepository.findByKey(configKey);
        if (configOpt.isPresent()) {
            return configOpt.get().getIntValue();
        }
        log.warn("[系统配置领域服务] 配置不存在 - 配置键: {}", configKey);
        return null;
    }
    
    /**
     * 获取配置值（布尔值）
     */
    public Boolean getConfigBooleanValue(String configKey) {
        Optional<SystemConfigEntity> configOpt = configRepository.findByKey(configKey);
        if (configOpt.isPresent()) {
            return configOpt.get().getBooleanValue();
        }
        log.warn("[系统配置领域服务] 配置不存在 - 配置键: {}", configKey);
        return null;
    }
    
    /**
     * 获取配置值（长整数）
     */
    public Long getConfigLongValue(String configKey) {
        Optional<SystemConfigEntity> configOpt = configRepository.findByKey(configKey);
        if (configOpt.isPresent()) {
            return configOpt.get().getLongValue();
        }
        log.warn("[系统配置领域服务] 配置不存在 - 配置键: {}", configKey);
        return null;
    }
    
    /**
     * 获取配置值（带默认值）
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 获取配置值（整数，带默认值）
     */
    public Integer getConfigIntValue(String configKey, Integer defaultValue) {
        Integer value = getConfigIntValue(configKey);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 获取配置值（布尔值，带默认值）
     */
    public Boolean getConfigBooleanValue(String configKey, Boolean defaultValue) {
        Boolean value = getConfigBooleanValue(configKey);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 更新配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String configKey, String configValue) {
        // 1. 验证配置键是否在白名单中
        if (!ALLOWED_CONFIG_KEYS.contains(configKey)) {
            log.error("[系统配置领域服务] 配置键不在允许修改的白名单中 - 配置键: {}", configKey);
            throw new BusinessException("不允许修改该配置: " + configKey);
        }
        
        // 2. 验证配置值格式
        validateConfigValue(configKey, configValue);
        
        // 3. 查询配置是否存在
        Optional<SystemConfigEntity> configOpt = configRepository.findByKey(configKey);
        if (configOpt.isPresent()) {
            SystemConfigEntity config = configOpt.get();
            config.updateValue(configValue);
            configRepository.update(config);
            log.info("[系统配置领域服务] 更新配置 - 配置键: {}, 配置值: {}", configKey, configValue);
        } else {
            throw new BusinessException("配置不存在: " + configKey);
        }
    }
    
    /**
     * 验证配置值格式
     */
    private void validateConfigValue(String configKey, String configValue) {
        if (configValue == null || configValue.trim().isEmpty()) {
            throw new BusinessException("配置值不能为空");
        }
        
        // 根据配置键验证对应的值格式
        switch (configKey) {
            case "private_message.enabled":
            case "private_message.allow_stranger":
                // 布尔类型配置：只能是 0 或 1
                if (!"0".equals(configValue) && !"1".equals(configValue)) {
                    throw new BusinessException("布尔类型配置值只能为 0 或 1");
                }
                break;
            case "private_message.max_message_length":
            case "private_message.rate_limit":
                // 整数类型配置：必须是正整数
                try {
                    int intValue = Integer.parseInt(configValue);
                    if (intValue <= 0) {
                        throw new BusinessException("数值类型配置必须为正整数");
                    }
                    // 私信最大长度限制：1-10000字符
                    if ("private_message.max_message_length".equals(configKey) && intValue > 10000) {
                        throw new BusinessException("私信最大长度不能超过10000字符");
                    }
                    // 频率限制：1-1000条/分钟
                    if ("private_message.rate_limit".equals(configKey) && intValue > 1000) {
                        throw new BusinessException("频率限制不能超过1000条/分钟");
                    }
                } catch (NumberFormatException e) {
                    throw new BusinessException("数值类型配置必须为有效整数");
                }
                break;
            default:
                // 未知配置键（不应该到达这里，因为已经过白名单验证）
                throw new BusinessException("未知的配置键: " + configKey);
        }
    }
    
    /**
     * 创建配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void createConfig(String configKey, String configValue, String configDesc) {
        Optional<SystemConfigEntity> configOpt = configRepository.findByKey(configKey);
        if (configOpt.isPresent()) {
            throw new BusinessException("配置已存在: " + configKey);
        }
        SystemConfigEntity config = SystemConfigEntity.create(configKey, configValue, configDesc);
        configRepository.save(config);
        log.info("[系统配置领域服务] 创建配置 - 配置键: {}, 配置值: {}", configKey, configValue);
    }
    
    /**
     * 获取所有配置
     */
    public List<SystemConfigEntity> getAllConfigs() {
        return configRepository.findAll();
    }
    
    /**
     * 根据键前缀获取配置
     */
    public List<SystemConfigEntity> getConfigsByPrefix(String keyPrefix) {
        return configRepository.findByKeyPrefix(keyPrefix);
    }
}

