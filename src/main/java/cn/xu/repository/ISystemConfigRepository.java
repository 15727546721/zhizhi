package cn.xu.repository;

import cn.xu.model.entity.SystemConfig;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置仓储接口
 * <p>定义系统配置数据的持久化操作</p>

 */
public interface ISystemConfigRepository {
    /**
     * 保存系统配置
     */
    void save(SystemConfig config);
    
    /**
     * 更新系统配置
     */
    void update(SystemConfig config);
    
    /**
     * 根据配置键查询配置
     */
    Optional<SystemConfig> findByKey(String configKey);
    
    /**
     * 查询所有配置
     */
    List<SystemConfig> findAll();
    
    /**
     * 根据配置键前缀查询配置
     */
    List<SystemConfig> findByKeyPrefix(String keyPrefix);
    
    /**
     * 删除配置
     */
    void deleteByKey(String configKey);
}
