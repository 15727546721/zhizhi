package cn.xu.repository;

import cn.xu.model.entity.UserMessageSettings;

import java.util.Optional;

/**
 * 用户私信设置仓储接口
 * <p>定义用户私信设置的持久化操作</p>

 */
public interface IUserMessageSettingsRepository {
    /**
     * 保存用户私信设置
     */
    void save(UserMessageSettings settings);
    
    /**
     * 更新用户私信设置
     */
    void update(UserMessageSettings settings);
    
    /**
     * 根据用户ID查询设置
     */
    Optional<UserMessageSettings> findByUserId(Long userId);
    
    /**
     * 删除用户私信设置
     */
    void deleteByUserId(Long userId);
}
