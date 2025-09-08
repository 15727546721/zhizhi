package cn.xu.infrastructure.config.satoken;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.infrastructure.persistent.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户权限验证接口实现
 * 
 * @author Lily
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserPermission implements StpInterface {

    private final PermissionRepository permissionRepository;

    private static final String ROLE_LIST_KEY = "ROLE_LIST";
    private static final String PERMISSION_LIST_KEY = "PERMISSION_LIST";

    /**
     * 获取权限码集合
     *
     * @param loginId   登录ID
     * @param loginType 登录类型
     * @return 权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        try {
            if (loginId == null || !StringUtils.hasText(String.valueOf(loginId))) {
                log.warn("获取权限列表失败：登录ID为空，loginType: {}", loginType);
                return Collections.emptyList();
            }

            // 从Session中获取权限列表，如果不存在则从数据库查询并缓存
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            return session.get(PERMISSION_LIST_KEY, () -> {
                Long userId = Long.valueOf(String.valueOf(loginId));
                
                // 获取通过角色关联的权限
                List<String> rolePermissions = new ArrayList<>(permissionRepository.findPermissionsByUserid(userId));
                
                // 获取用户直接关联的权限（这里需要实现具体的查询逻辑）
                List<String> directPermissions = getDirectPermissionsByUserId(userId);
                
                // 合并两种权限，去重
                List<String> allPermissions = new ArrayList<>();
                allPermissions.addAll(rolePermissions);
                allPermissions.addAll(directPermissions);
                
                // 去重
                List<String> distinctPermissions = allPermissions.stream().distinct().collect(Collectors.toList());
                
                log.info("用户 [{}] 加载权限列表: 角色权限{}个, 直接权限{}个, 合计{}个", 
                        loginId, rolePermissions.size(), directPermissions.size(), distinctPermissions.size());
                log.debug("用户 [{}] 权限列表加载完成: {}", loginId, distinctPermissions);
                return distinctPermissions;
            });
        } catch (Exception e) {
            log.error("获取用户权限列表异常, loginId: {}, loginType: {}", loginId, loginType, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户直接关联的权限
     * 
     * @param userId 用户ID
     * @return 直接权限列表
     */
    private List<String> getDirectPermissionsByUserId(Long userId) {
        // 获取用户直接关联的权限
        return permissionRepository.findDirectPermissionsByUserid(userId);
    }

    /**
     * 获取角色标识集合
     *
     * @param loginId   登录ID
     * @param loginType 登录类型
     * @return 角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            if (loginId == null || !StringUtils.hasText(String.valueOf(loginId))) {
                log.warn("获取角色列表失败：登录ID为空，loginType: {}", loginType);
                return Collections.emptyList();
            }

            // 从Session中获取角色列表，如果不存在则从数据库查询并缓存
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            return session.get(ROLE_LIST_KEY, () -> {
                Long userId = Long.valueOf(String.valueOf(loginId));
                List<String> roles = permissionRepository.findRolesByUserid(userId);

                log.info("用户 [{}] 加载角色列表: {}", loginId, roles);
                log.debug("用户 [{}] 角色列表加载完成，共 {} 个角色", loginId, roles.size());
                return roles;
            });
        } catch (Exception e) {
            log.error("获取用户角色列表异常, loginId: {}, loginType: {}", loginId, loginType, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 清除用户权限缓存
     * @param userId 用户ID
     */
    public void clearUserPermissionCache(Long userId) {
        try {
            SaSession session = StpUtil.getSessionByLoginId(userId, false);
            if (session != null) {
                session.delete(ROLE_LIST_KEY);
                session.delete(PERMISSION_LIST_KEY);
                log.info("用户 [{}] 权限缓存已清除", userId);
            } else {
                log.debug("用户 [{}] 会话不存在，无需清除权限缓存", userId);
            }
        } catch (Exception e) {
            log.warn("清除用户权限缓存异常, userId: {}", userId, e);
        }
    }
    
    /**
     * 记录权限检查日志
     * @param userId 用户ID
     * @param permission 权限标识
     * @param hasPermission 是否有权限
     */
    public void logPermissionCheck(Long userId, String permission, boolean hasPermission) {
        if (hasPermission) {
            log.info("用户 [{}] 权限检查通过: {}", userId, permission);
        } else {
            log.warn("用户 [{}] 权限检查拒绝: {}", userId, permission);
        }
    }
    
    /**
     * 记录角色检查日志
     * @param userId 用户ID
     * @param role 角色标识
     * @param hasRole 是否有角色
     */
    public void logRoleCheck(Long userId, String role, boolean hasRole) {
        if (hasRole) {
            log.info("用户 [{}] 角色检查通过: {}", userId, role);
        } else {
            log.warn("用户 [{}] 角色检查拒绝: {}", userId, role);
        }
    }
}