package cn.xu.domain.user.service;

import java.util.List;

/**
 * 用户角色服务接口
 * 
 * @author Lily
 */
public interface IUserRoleService {
    
    /**
     * 为用户分配角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);
    
    /**
     * 获取用户的角色ID列表
     * 
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);
}