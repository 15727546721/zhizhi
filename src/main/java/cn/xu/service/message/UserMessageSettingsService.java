package cn.xu.service.message;

import cn.xu.model.entity.UserMessageSettings;
import cn.xu.repository.IUserMessageSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户私信设置服务
 *
 * @author xu
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserMessageSettingsService {
    
    private final IUserMessageSettingsRepository settingsRepository;
    private final SystemConfigService systemConfigService;
    
    /**
     * 获取用户私信设置（如果不存在则创建默认设置）
     */
    public UserMessageSettings getOrCreateDefaultSettings(Long userId) {
        Optional<UserMessageSettings> settingsOpt = settingsRepository.findByUserId(userId);
        if (settingsOpt.isPresent()) {
            return settingsOpt.get();
        }
        
        // 从系统配置获取默认值
        Boolean systemAllowStranger = systemConfigService.getConfigBooleanValue(
                "private_message.allow_stranger", true);
        
        // 创建默认设置
        UserMessageSettings defaultSettings = UserMessageSettings.createDefault(
                userId, systemAllowStranger != null && systemAllowStranger);
        settingsRepository.save(defaultSettings);
        log.info("为用户创建默认私信设置 - userId: {}, allowStranger: {}", userId, systemAllowStranger);
        return defaultSettings;
    }
    
    /**
     * 更新用户私信设置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSettings(Long userId, Boolean allowStrangerMessage, 
                               Boolean allowNonMutualFollowMessage, Boolean messageNotificationEnabled) {
        UserMessageSettings settings = getOrCreateDefaultSettings(userId);
        settings.updateSettings(allowStrangerMessage, allowNonMutualFollowMessage, messageNotificationEnabled);
        settingsRepository.update(settings);
        log.info("更新用户私信设置 - userId: {}", userId);
    }
    
    /**
     * 获取用户私信设置
     */
    public UserMessageSettings getSettings(Long userId) {
        return getOrCreateDefaultSettings(userId);
    }
}
