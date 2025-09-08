package cn.xu.domain.user.service;

import java.util.List;

/**
 * 用户权限服务接口
 * 负责管理用户与权限的直接关联关系
 * 
 * @author Lily
 */
public interface IUserPermissionService {
    
    /**
     * 为用户直接分配权限
     * 
     * @param userId 用户ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissionsToUser(Long userId, List<Long> permissionIds);
    
    /**
     * 获取用户的直接权限ID列表
     * 
     * @param userId 用户ID
     * @return 权限ID列表
     */
    List<Long> getUserPermissionIds(Long userId);
    
    /**
     * 移除用户的直接权限
     * 
     * @param userId 用户ID
     * @param permissionIds 权限ID列表
     */
    void removeUserPermissions(Long userId, List<Long> permissionIds);
    
    /**
     * 清除用户的所有直接权限
     * 
     * @param userId 用户ID
     */
    void clearUserPermissions(Long userId);
}