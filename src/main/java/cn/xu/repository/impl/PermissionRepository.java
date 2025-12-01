package cn.xu.repository.impl;

import cn.xu.common.ResponseCode;
import cn.xu.common.response.PageResponse;
import cn.xu.model.entity.Menu;
import cn.xu.model.entity.Role;
import cn.xu.repository.IPermissionRepository;
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
 * 直接使用PO，简化架构，移除过度的Entity转换层
 * 
 * @author xu
 * @since 2025-11-29
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PermissionRepository implements IPermissionRepository {

    private final RoleMapper roleDao;
    private final MenuMapper menuDao;
    private final TransactionTemplate transactionTemplate;

    @Override
    public List<String> findRolesByUserId(Long userId) {
        return roleDao.selectRolesByUserid(userId);
    }

    @Override
    public List<Menu> findAllMenus() {
        return menuDao.selectMenuList();
    }

    @Override
    public Optional<Menu> findMenuById(Long id) {
        Menu menu = menuDao.selectMenuById(id);
        return Optional.ofNullable(menu);
    }

    @Override
    public PageResponse<List<Role>> findRolePage(String name, int offset, int size) {
        List<Role> roleList = roleDao.selectRolePage(name, offset, size);
        long total = roleDao.countRole(name);
        return PageResponse.ofList(offset / size + 1, size, total, roleList);
    }

    @Override
    public Optional<Long> findRoleIdByUserId(long userId) {
        Long roleId = roleDao.selectRoleIdByUserId(userId);
        return Optional.ofNullable(roleId);
    }

    @Override
    public List<Long> findMenuIdsByRoleId(Long roleId) {
        return roleDao.selectMenuIdByRoleMenu(roleId);
    }

    @Override
    public Optional<Role> findRoleById(Long roleId) {
        Role role = roleDao.selectRoleById(roleId);
        return Optional.ofNullable(role);
    }

    @Override
    public List<Long> findAllMenuIds() {
        return menuDao.selectAllMenuId();
    }

    @Override
    public void deleteRoleMenuByRoleId(Long roleId) {
        roleDao.deleteRoleMenuByRoleId(roleId);
    }

    @Override
    public void insertRoleMenu(Long roleId, List<Long> menuIds) {
        roleDao.insertRoleMenu(roleId, menuIds);
    }

    @Override
    public Role saveRole(Role role) {
        try {
            if (role.getId() == null) {
                roleDao.insertRole(role);
            } else {
                roleDao.updateRole(role);
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
                roleDao.deleteRoleByIds(ids);
                roleDao.deleteRoleMenuByRoleIds(ids);
                return true;
            } catch (Exception e) {
                // 如果出现异常，可以设置事务回滚
                status.setRollbackOnly();
                // 处理异常
                log.error("删除角色失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除角色失败");
            }
        });
    }

    @Override
    public void saveMenu(Menu menu) {
        if (menu.getId() == null) {
            menuDao.addMenu(menu);
        } else {
            menuDao.updateMenu(menu);
        }
    }

    @Override
    public void deleteMenu(Long id) {
        menuDao.deleteMenu(id);
    }

    @Override
    public List<Long> findMenuIdsByUserId(long userId) {
        return menuDao.getMenuById(userId);
    }

    @Override
    public List<Menu> findMenusByIds(List<Long> menuIds) {
        return menuDao.listByIds(menuIds);
    }

    @Override
    public List<String> findPermissionsByUserId(Long userId) {
        return menuDao.findPermissionsByUserid(userId);
    }

    @Override
    public boolean existsByCode(String code) {
        return roleDao.countByCode(code) > 0;
    }

    @Override
    public List<Long> findUserIdsByRoleId(Long roleId) {
        return roleDao.findUserIdsByRoleId(roleId);
    }
}