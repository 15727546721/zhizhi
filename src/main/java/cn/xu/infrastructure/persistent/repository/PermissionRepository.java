package cn.xu.infrastructure.persistent.repository;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.repository.IPermissionRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.converter.MenuConverter;
import cn.xu.infrastructure.persistent.converter.RoleConverter;
import cn.xu.infrastructure.persistent.dao.MenuMapper;
import cn.xu.infrastructure.persistent.dao.RoleMapper;
import cn.xu.infrastructure.persistent.po.Menu;
import cn.xu.infrastructure.persistent.po.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * 权限仓储实现类
 * 通过Converter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PermissionRepository implements IPermissionRepository {

    private final RoleMapper roleDao;
    private final MenuMapper menuDao;
    private final TransactionTemplate transactionTemplate;
    private final RoleConverter roleConverter;
    private final MenuConverter menuConverter;

    @Override
    public List<String> findRolesByUserid(Long userid) {
        List<String> roles = roleDao.selectRolesByUserid(userid);
        return roles;
    }

    @Override
    public List<MenuEntity> selectMenuList() {
        List<Menu> menuList = menuDao.selectMenuList();
        return menuConverter.toDomainEntities(menuList);
    }

    @Override
    public MenuEntity selectMenuById(Long id) {
        Menu menu = menuDao.selectMenuById(id);
        return menuConverter.toDomainEntity(menu);
    }

    @Override
    public List<RoleEntity> selectRolePage(String name, int page, int size) {
        int offset = Math.max(0, (page - 1) * size);
        List<Role> roleList = roleDao.selectRolePage(name, offset, size);
        return roleConverter.toDomainEntities(roleList);
    }

    @Override
    public long countRole(String name) {
        long count = roleDao.countRole(name);
        return count;
    }

    @Override
    public Long selectRoleIdByUserId(long userId) {
        return roleDao.selectRoleIdByUserId(userId);
    }

    @Override
    public List<Long> selectMenuIdByRoleMenu(Long roleId) {
        return roleDao.selectMenuIdByRoleMenu(roleId);
    }

    @Override
    public RoleEntity selectRoleById(Long roleId) {
        Role role = roleDao.selectRoleById(roleId);
        return roleConverter.toDomainEntity(role);
    }

    @Override
    public List<Long> selectAllMenuId() {
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
    public void addRole(RoleEntity roleEntity) {
        Role role = roleConverter.toDataObject(roleEntity);
        try {
            roleDao.insertRole(role);
        } catch (Exception e) {
            log.error("新增角色失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "新增角色失败");
        }
    }

    @Override
    public void updateRole(RoleEntity roleEntity) {
        try {
            Role role = roleConverter.toDataObject(roleEntity);
            roleDao.updateRole(role);
        } catch (Exception e) {
            log.error("更新角色失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新角色失败");
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
    public void addMenu(MenuEntity menuEntity) {
        Menu menu = menuConverter.toDataObject(menuEntity);
        menuDao.addMenu(menu);
    }

    @Override
    public void updateMenu(MenuEntity menuEntity) {
        Menu menu = menuConverter.toDataObject(menuEntity);
        menuDao.updateMenu(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        menuDao.deleteMenu(id);
    }

    @Override
    public List<Long> getMenuById(long userId) {
        return menuDao.getMenuById(userId);
    }

    @Override
    public List<MenuEntity> listByIds(List<Long> menuIds) {
        List<Menu> menuList = menuDao.listByIds(menuIds);
        return menuConverter.toDomainEntities(menuList);
    }

    @Override
    public List<String> findPermissionsByUserid(Long userId) {
        return menuDao.findPermissionsByUserid(userId);
    }

    @Override
    public boolean existsByCode(String code) {
        return roleDao.countByCode(code) > 0;
    }
}
