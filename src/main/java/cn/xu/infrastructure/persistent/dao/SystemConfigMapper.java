package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统配置Mapper
 */
@Mapper
public interface SystemConfigMapper {
    /**
     * 插入系统配置
     */
    int insert(SystemConfig config);
    
    /**
     * 更新系统配置
     */
    int update(SystemConfig config);
    
    /**
     * 根据配置键查询配置
     */
    SystemConfig selectByKey(@Param("configKey") String configKey);
    
    /**
     * 查询所有配置
     */
    List<SystemConfig> selectAll();
    
    /**
     * 根据配置键前缀查询配置
     */
    List<SystemConfig> selectByKeyPrefix(@Param("keyPrefix") String keyPrefix);
    
    /**
     * 删除配置
     */
    int deleteByKey(@Param("configKey") String configKey);
}

