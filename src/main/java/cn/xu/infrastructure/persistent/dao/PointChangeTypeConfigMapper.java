package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.PointChangeTypeConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 积分变动类型配置Mapper接口
 */
@Mapper
public interface PointChangeTypeConfigMapper {
    
    /**
     * 根据变动类型查询配置信息
     */
    PointChangeTypeConfig findByChangeType(String changeType);
    
    /**
     * 查询所有启用的积分变动类型配置
     */
    List<PointChangeTypeConfig> findAllActive();
}