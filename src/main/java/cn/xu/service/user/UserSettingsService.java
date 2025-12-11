package cn.xu.service.user;

import cn.xu.common.ResponseCode;
import cn.xu.model.entity.UserSettings;
import cn.xu.repository.IUserSettingsRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户设置服务
 * <p>负责用户设置的获取、更新等功能</p>
 
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSettingsService {

    private final IUserSettingsRepository settingsRepository;

    /**
     * 获取或创建默认用户设置
     */
    public UserSettings getOrCreateDefaultSettings(Long userId) {
        Optional<UserSettings> settingsOpt = settingsRepository.findByUserId(userId);
        if (settingsOpt.isPresent()) {
            return settingsOpt.get();
        }

        // 创建默认设置
        UserSettings defaultSettings = UserSettings.createDefault(userId);
        settingsRepository.save(defaultSettings);
        log.info("创建默认用户设置 - userId: {}", userId);
        return defaultSettings;
    }

    /**
     * 获取用户设置
     */
    public UserSettings getSettings(Long userId) {
        return getOrCreateDefaultSettings(userId);
    }

    /**
     * 更新隐私设置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivacySettings(Long userId, Integer profileVisibility, Boolean showOnlineStatus) {
        UserSettings settings = getOrCreateDefaultSettings(userId);
        settings.updatePrivacySettings(profileVisibility, showOnlineStatus);
        settingsRepository.update(settings);
        log.info("更新隐私设置 - userId: {}", userId);
    }

    /**
     * 更新通知设置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateNotificationSettings(Long userId, Boolean emailNotification,
                                           Boolean browserNotification, Boolean soundNotification) {
        UserSettings settings = getOrCreateDefaultSettings(userId);
        settings.updateNotificationSettings(emailNotification, browserNotification, soundNotification);
        settingsRepository.update(settings);
        log.info("更新通知设置 - userId: {}", userId);
    }

    /**
     * 设置邮箱验证Token
     */
    @Transactional(rollbackFor = Exception.class)
    public void setEmailVerifyToken(Long userId, String token, LocalDateTime expireTime) {
        getOrCreateDefaultSettings(userId);
        settingsRepository.setEmailVerifyToken(userId, token, expireTime);
        log.info("设置邮箱验证Token - userId: {}", userId);
    }

    /**
     * 验证邮箱
     */
    @Transactional(rollbackFor = Exception.class)
    public void verifyEmail(String token) {
        Optional<UserSettings> settingsOpt = settingsRepository.findByEmailVerifyToken(token);
        if (!settingsOpt.isPresent()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱验证Token无效");
        }

        UserSettings settings = settingsOpt.get();
        if (!settings.isEmailVerifyTokenValid()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱验证Token已过期");
        }

        settingsRepository.clearEmailVerifyToken(settings.getUserId());
        log.info("邮箱验证成功 - userId: {}", settings.getUserId());
    }
}
