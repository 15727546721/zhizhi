package cn.xu.config.satoken;

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


/**
 * 用户权限验证接口实现
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
                return Collections.emptyList();
            }

            // 从Session中获取权限列表，如果不存在则从数据库查询并缓存
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            return session.get(PERMISSION_LIST_KEY, () -> {
                Long userId = Long.valueOf(String.valueOf(loginId));
                List<String> permissions = new ArrayList<>(permissionRepository.findPermissionsByUserid(userId));

                log.info("用户 [{}] 加载权限列表: {}", loginId, permissions);
                return permissions;
            });
        } catch (Exception e) {
            log.error("获取用户权限列表异常, loginId: {}", loginId, e);
            return Collections.emptyList();
        }
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
                return Collections.emptyList();
            }

            // 从Session中获取角色列表，如果不存在则从数据库查询并缓存
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            return session.get(ROLE_LIST_KEY, () -> {
                Long userId = Long.valueOf(String.valueOf(loginId));
                List<String> roles = permissionRepository.findRolesByUserid(userId);

                log.info("用户 [{}] 加载角色列表: {}", loginId, roles);
                return roles;
            });
        } catch (Exception e) {
            log.error("获取用户角色列表异常, loginId: {}", loginId, e);
            return Collections.emptyList();
        }
    }

}

