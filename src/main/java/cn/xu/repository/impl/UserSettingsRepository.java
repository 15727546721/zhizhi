package cn.xu.repository.impl;

import cn.xu.model.entity.UserSettings;
import cn.xu.repository.IUserSettingsRepository;
import cn.xu.repository.mapper.UserSettingsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户设置仓储实现
 * <p>负责用户设置数据的持久化操作</p>
 
 */
@Repository
@RequiredArgsConstructor
public class UserSettingsRepository implements IUserSettingsRepository {
    
    private final UserSettingsMapper mapper;
    
    @Override
    public void save(UserSettings settings) {
        if (settings.getId() == null) {
            mapper.insert(settings);
        } else {
            mapper.update(settings);
        }
    }
    
    @Override
    public void update(UserSettings settings) {
        mapper.update(settings);
    }
    
    @Override
    public Optional<UserSettings> findByUserId(Long userId) {
        return Optional.ofNullable(mapper.selectByUserId(userId));
    }
    
    @Override
    public Optional<UserSettings> findByEmailVerifyToken(String token) {
        return Optional.ofNullable(mapper.selectByEmailVerifyToken(token));
    }
    
    @Override
    public void setEmailVerifyToken(Long userId, String token, java.time.LocalDateTime expireTime) {
        mapper.setEmailVerifyToken(userId, token, expireTime);
    }
    
    @Override
    public void clearEmailVerifyToken(Long userId) {
        mapper.clearEmailVerifyToken(userId);
    }
    
    @Override
    public void setPasswordResetToken(Long userId, String token, java.time.LocalDateTime expireTime) {
        mapper.setPasswordResetToken(userId, token, expireTime);
    }
    
    @Override
    public Optional<UserSettings> findByPasswordResetToken(String token) {
        return Optional.ofNullable(mapper.selectByPasswordResetToken(token));
    }
    
    @Override
    public void clearPasswordResetToken(Long userId) {
        mapper.clearPasswordResetToken(userId);
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        mapper.deleteByUserId(userId);
    }
}

