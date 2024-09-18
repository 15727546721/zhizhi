package cn.xu.config.satoken;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.infrastructure.persistent.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * 用户权限验证接口实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserPermission implements StpInterface {

    @Resource
    private PermissionRepository permissionRepository;

    /**
     * 返回一个账号所拥有的权限码集合
     * 即你在调用 StpUtil.login(id) 时写入的标识值。
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本list仅做模拟，实际项目中要根据具体业务逻辑来查询权限
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("user-add");
        list.add("user-delete");
        list.add("user-update");
        list.add("user-get");
        list.add("article-get");
        log.info("用户权限列表：{}", list);
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSessionByLoginId(loginId);
        List<String> roleList = session.get("Role_List", () -> {
            // 从数据库查询这个账号id拥有的角色列表
            return permissionRepository.findRolesByUserid(Long.valueOf((String) loginId));
        });
        log.info("用户id：{} 用户角色列表：{}", loginId, roleList);
        return  roleList;
    }

}

