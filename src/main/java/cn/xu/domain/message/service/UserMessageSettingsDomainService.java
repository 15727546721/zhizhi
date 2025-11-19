package cn.xu.domain.message.service;

import cn.xu.domain.message.model.entity.UserMessageSettingsEntity;
import cn.xu.domain.message.repository.IUserMessageSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户私信设置领域服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserMessageSettingsDomainService {
    
    private final IUserMessageSettingsRepository settingsRepository;
    private final SystemConfigDomainService systemConfigDomainService;
    
    /**
     * 获取用户私信设置（如果不存在则创建默认设置）
     * 默认设置使用系统配置的默认值
     */
    public UserMessageSettingsEntity getOrCreateDefaultSettings(Long userId) {
        Optional<UserMessageSettingsEntity> settingsOpt = settingsRepository.findByUserId(userId);
        if (settingsOpt.isPresent()) {
            return settingsOpt.get();
        }
        
        // 从系统配置获取默认值
        Boolean systemAllowStranger = systemConfigDomainService.getConfigBooleanValue(
                "private_message.allow_stranger", true);
        
        // 创建默认设置（使用系统默认值）
        UserMessageSettingsEntity defaultSettings = UserMessageSettingsEntity.createDefault(
                userId, systemAllowStranger != null && systemAllowStranger);
        settingsRepository.save(defaultSettings);
        log.info("[用户私信设置领域服务] 为用户创建默认设置 - 用户ID: {}, 系统默认允许陌生人: {}", 
                userId, systemAllowStranger);
        return defaultSettings;
    }
    
    /**
     * 更新用户私信设置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSettings(Long userId, Boolean allowStrangerMessage, Boolean allowNonMutualFollowMessage, Boolean messageNotificationEnabled) {
        UserMessageSettingsEntity settings = getOrCreateDefaultSettings(userId);
        settings.updateSettings(allowStrangerMessage, allowNonMutualFollowMessage, messageNotificationEnabled);
        settingsRepository.update(settings);
        log.info("[用户私信设置领域服务] 更新用户私信设置 - 用户ID: {}", userId);
    }
    
    /**
     * 获取用户私信设置
     */
    public UserMessageSettingsEntity getSettings(Long userId) {
        return getOrCreateDefaultSettings(userId);
    }
}

