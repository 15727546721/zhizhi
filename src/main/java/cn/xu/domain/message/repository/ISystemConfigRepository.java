package cn.xu.domain.message.repository;

import cn.xu.domain.message.model.entity.SystemConfigEntity;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置仓储接口
 */
public interface ISystemConfigRepository {
    /**
     * 保存系统配置
     */
    void save(SystemConfigEntity config);
    
    /**
     * 更新系统配置
     */
    void update(SystemConfigEntity config);
    
    /**
     * 根据配置键查询配置
     */
    Optional<SystemConfigEntity> findByKey(String configKey);
    
    /**
     * 查询所有配置
     */
    List<SystemConfigEntity> findAll();
    
    /**
     * 根据配置键前缀查询配置
     */
    List<SystemConfigEntity> findByKeyPrefix(String keyPrefix);
    
    /**
     * 删除配置
     */
    void deleteByKey(String configKey);
}

