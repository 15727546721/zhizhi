package cn.xu.repository.impl;

import cn.xu.common.ResponseCode;
import cn.xu.common.response.PageResponse;
import cn.xu.model.entity.Menu;
import cn.xu.model.entity.Role;
import cn.xu.repository.PermissionRepository;
import cn.xu.repository.mapper.MenuMapper;
import cn.xu.repository.mapper.RoleMapper;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

/**
 * 权限仓储实现类
 * <p>负责角色、菜单权限的持久化操作</p>

 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final TransactionTemplate transactionTemplate;

    @Override
    public List<String> findRolesByUserId(Long userId) {
        return roleMapper.selectRolesByUserid(userId);
    }

    @Override
    public List<Menu> findAllMenus() {
        return menuMapper.selectMenuList();
    }

    @Override
    public Optional<Menu> findMenuById(Long id) {
        Menu menu = menuMapper.selectMenuById(id);
        return Optional.ofNullable(menu);
    }

    @Override
    public PageResponse<List<Role>> findRolePage(String name, int offset, int size) {
        // 防止除零
        if (size <= 0) {
            size = 10;
        }
        List<Role> roleList = roleMapper.selectRolePage(name, offset, size);
        long total = roleMapper.countRole(name);
        return PageResponse.ofList(offset / size + 1, size, total, roleList);
    }

    @Override
    public Optional<Long> findRoleIdByUserId(long userId) {
        Long roleId = roleMapper.selectRoleIdByUserId(userId);
        return Optional.ofNullable(roleId);
    }

    @Override
    public List<Long> findMenuIdsByRoleId(Long roleId) {
        return roleMapper.selectMenuIdByRoleMenu(roleId);
    }

    @Override
    public Optional<Role> findRoleById(Long roleId) {
        Role role = roleMapper.selectRoleById(roleId);
        return Optional.ofNullable(role);
    }

    @Override
    public List<Long> findAllMenuIds() {
        return menuMapper.selectAllMenuId();
    }

    @Override
    public void deleteRoleMenuByRoleId(Long roleId) {
        roleMapper.deleteRoleMenuByRoleId(roleId);
    }

    @Override
    public void insertRoleMenu(Long roleId, List<Long> menuIds) {
        roleMapper.insertRoleMenu(roleId, menuIds);
    }

    @Override
    public Role saveRole(Role role) {
        try {
            if (role.getId() == null) {
                roleMapper.insertRole(role);
            } else {
                roleMapper.updateRole(role);
            }
            return role;
        } catch (Exception e) {
            log.error("保存角色失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存角色失败");
        }
    }

    @Override
    public void deleteRoleByIds(List<Long> ids) {
        transactionTemplate.execute(status -> {
            try {
                roleMapper.deleteRoleByIds(ids);
                roleMapper.deleteRoleMenuByRoleIds(ids);
                return true;
            } catch (Exception e) {
                // 如果出现异常，设置事务回滚
                status.setRollbackOnly();
                log.error("删除角色失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除角色失败");
            }
        });
    }

    @Override
    public void saveMenu(Menu menu) {
        if (menu.getId() == null) {
            menuMapper.addMenu(menu);
        } else {
            menuMapper.updateMenu(menu);
        }
    }

    @Override
    public void deleteMenu(Long id) {
        menuMapper.deleteMenu(id);
    }

    @Override
    public List<Long> findMenuIdsByUserId(long userId) {
        return menuMapper.getMenuById(userId);
    }

    @Override
    public List<Menu> findMenusByIds(List<Long> menuIds) {
        return menuMapper.listByIds(menuIds);
    }

    @Override
    public List<String> findPermissionsByUserId(Long userId) {
        return menuMapper.findPermissionsByUserid(userId);
    }

    @Override
    public boolean existsByCode(String code) {
        return roleMapper.countByCode(code) > 0;
    }

    @Override
    public List<Long> findUserIdsByRoleId(Long roleId) {
        return roleMapper.findUserIdsByRoleId(roleId);
    }
}
