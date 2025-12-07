package cn.xu.service.user;

import cn.xu.config.satoken.UserPermission;
import cn.xu.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户角色服务
 * 负责用户与角色的关联管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {
    
    @Resource
    private IUserRepository userRepository;
    
    @Resource
    private UserPermission userPermission;
    
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        userRepository.assignRolesToUser(userId, roleIds);
        // 清除用户的权限缓存
        userPermission.clearUserPermissionCache(userId);
        log.info("为用户分配角色成功: userId: {}, roleIds: {}", userId, roleIds);
    }
    
    public List<Long> getUserRoleIds(Long userId) {
        return userRepository.findRoleIdsByUserId(userId);
    }
}