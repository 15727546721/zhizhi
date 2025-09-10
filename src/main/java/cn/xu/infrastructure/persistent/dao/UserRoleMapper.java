package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色Mapper接口
 * 
 * @author Lily
 */
@Mapper
public interface UserRoleMapper {
    int insert(UserRole userRole);

    int deleteUserRoleByUserId(Long userId);
    
    /**
     * 批量保存用户角色关联
     */
    void saveUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
    
    /**
     * 删除用户角色关联
     */
    void deleteByUserId(@Param("userId") Long userId);
}