package cn.xu.repository.mapper;

import cn.xu.model.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统配置Mapper接口
 * <p>处理系统配置的数据库操作</p>

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
