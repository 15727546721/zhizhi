package cn.xu.repository.mapper;

import cn.xu.model.entity.UserMessageSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户私信设置Mapper
 */
@Mapper
public interface UserMessageSettingsMapper {
    /**
     * 插入用户私信设置
     */
    int insert(UserMessageSettings settings);
    
    /**
     * 更新用户私信设置
     */
    int update(UserMessageSettings settings);
    
    /**
     * 根据用户ID查询设置
     */
    UserMessageSettings selectByUserId(@Param("userId") Long userId);
    
    /**
     * 删除用户私信设置
     */
    int deleteByUserId(@Param("userId") Long userId);
}

