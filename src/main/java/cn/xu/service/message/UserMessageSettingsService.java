package cn.xu.service.message;

import cn.xu.model.entity.UserSettings;
import cn.xu.service.user.UserSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户私信设置服务（委托给UserSettingsService）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserMessageSettingsService {
    
    private final UserSettingsService userSettingsService;
    
    /**
     * 获取用户私信设置
     */
    public UserSettings getSettings(Long userId) {
        return userSettingsService.getOrCreateDefaultSettings(userId);
    }
    
    /**
     * 更新用户私信设置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSettings(Long userId, Boolean allowStrangerMessage) {
        userSettingsService.updateMessageSettings(userId, allowStrangerMessage);
        log.info("更新用户私信设置 - userId: {}", userId);
    }
    
    /**
     * 检查是否允许陌生人私信
     */
    public boolean isAllowStrangerMessage(Long userId) {
        return userSettingsService.isAllowStrangerMessage(userId);
    }
}
