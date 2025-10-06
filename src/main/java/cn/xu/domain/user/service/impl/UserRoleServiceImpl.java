package cn.xu.domain.user.service.impl;

import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserRoleService;
import cn.xu.infrastructure.config.satoken.UserPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户角色服务实现类
 * 
 * @author Lily
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements IUserRoleService {
    
    @Resource
    private IUserRepository userRepository;
    
    @Resource
    private UserPermission userPermission;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        userRepository.assignRolesToUser(userId, roleIds);
        // 清除用户的权限缓存
        userPermission.clearUserPermissionCache(userId);
        log.info("为用户分配角色成功, userId: {}, roleIds: {}", userId, roleIds);
    }
    
    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return userRepository.findRoleIdsByUserId(userId);
    }
}