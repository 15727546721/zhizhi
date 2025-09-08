package cn.xu.domain.user.service.impl;

import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserPermissionService;
import cn.xu.infrastructure.config.satoken.UserPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户权限服务实现类
 * 负责管理用户与权限的直接关联关系
 * 
 * @author Lily
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPermissionServiceImpl implements IUserPermissionService {
    
    @Resource
    private IUserRepository userRepository;
    
    @Resource
    private UserPermission userPermission;
    
    @Override
    public void assignPermissionsToUser(Long userId, List<Long> permissionIds) {
        userRepository.assignPermissionsToUser(userId, permissionIds);
        // 清除用户的权限缓存
        userPermission.clearUserPermissionCache(userId);
        log.info("为用户直接分配权限成功, userId: {}, permissionIds: {}", userId, permissionIds);
    }
    
    @Override
    public List<Long> getUserPermissionIds(Long userId) {
        return userRepository.findPermissionIdsByUserId(userId);
    }
    
    @Override
    public void removeUserPermissions(Long userId, List<Long> permissionIds) {
        userRepository.removeUserPermissions(userId, permissionIds);
        // 清除用户的权限缓存
        userPermission.clearUserPermissionCache(userId);
        log.info("移除用户的直接权限成功, userId: {}, permissionIds: {}", userId, permissionIds);
    }
    
    @Override
    public void clearUserPermissions(Long userId) {
        userRepository.removeUserPermissions(userId, getUserPermissionIds(userId));
        // 清除用户的权限缓存
        userPermission.clearUserPermissionCache(userId);
        log.info("清除用户的所有直接权限成功, userId: {}", userId);
    }
}