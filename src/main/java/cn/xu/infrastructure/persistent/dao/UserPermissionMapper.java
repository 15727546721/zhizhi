package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.UserPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户权限Mapper接口
 * 
 * @author Lily
 */
@Mapper
public interface UserPermissionMapper {
    
    /**
     * 批量保存用户权限关联
     */
    void saveUserPermissions(@Param("userId") Long userId, @Param("permissionIds") List<Long> permissionIds);
    
    /**
     * 删除用户权限关联
     */
    void deleteUserPermissions(@Param("userId") Long userId, @Param("permissionIds") List<Long> permissionIds);
    
    /**
     * 清除用户的所有权限关联
     */
    void clearUserPermissions(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询权限ID列表
     */
    List<Long> findPermissionIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和权限ID列表查询用户权限关联
     */
    List<UserPermission> findUserPermissions(@Param("userId") Long userId, @Param("permissionIds") List<Long> permissionIds);
}