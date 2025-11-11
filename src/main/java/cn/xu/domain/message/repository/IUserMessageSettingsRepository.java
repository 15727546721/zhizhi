package cn.xu.domain.message.repository;

import cn.xu.domain.message.model.entity.UserMessageSettingsEntity;

import java.util.Optional;

/**
 * 用户私信设置仓储接口
 */
public interface IUserMessageSettingsRepository {
    /**
     * 保存用户私信设置
     */
    void save(UserMessageSettingsEntity settings);
    
    /**
     * 更新用户私信设置
     */
    void update(UserMessageSettingsEntity settings);
    
    /**
     * 根据用户ID查询设置
     */
    Optional<UserMessageSettingsEntity> findByUserId(Long userId);
    
    /**
     * 删除用户私信设置
     */
    void deleteByUserId(Long userId);
}

