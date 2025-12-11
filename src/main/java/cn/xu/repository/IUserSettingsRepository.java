package cn.xu.repository;

import cn.xu.model.entity.UserSettings;

import java.util.Optional;

/**
 * 用户设置仓储接口
 * <p>定义用户设置的持久化操作</p>
 
 */
public interface IUserSettingsRepository {
    
    void save(UserSettings settings);
    
    void update(UserSettings settings);
    
    Optional<UserSettings> findByUserId(Long userId);
    
    Optional<UserSettings> findByEmailVerifyToken(String token);
    
    void setEmailVerifyToken(Long userId, String token, java.time.LocalDateTime expireTime);
    
    void clearEmailVerifyToken(Long userId);
    
    void setPasswordResetToken(Long userId, String token, java.time.LocalDateTime expireTime);
    
    Optional<UserSettings> findByPasswordResetToken(String token);
    
    void clearPasswordResetToken(Long userId);
    
    void deleteByUserId(Long userId);
}

