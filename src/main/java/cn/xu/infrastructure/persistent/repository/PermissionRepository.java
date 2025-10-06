package cn.xu.infrastructure.persistent.repository;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.PageResponse;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.repository.IPermissionRepository;
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
import java.util.Optional;

/**
 * 权限仓储实现类
 * 通过Converter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 * 
 * @author Lily
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
    public List<String> findRolesByUserId(Long userId) {
        return roleDao.selectRolesByUserid(userId);
    }

    @Override
    public List<MenuEntity> findAllMenus() {
        List<Menu> menuList = menuDao.selectMenuList();
        return menuConverter.toDomainEntities(menuList);
    }

    @Override
    public Optional<MenuEntity> findMenuById(Long id) {
        Menu menu = menuDao.selectMenuById(id);
        return Optional.ofNullable(menuConverter.toDomainEntity(menu));
    }

    @Override
    public PageResponse<List<RoleEntity>> findRolePage(String name, int offset, int size) {
        List<Role> roleList = roleDao.selectRolePage(name, offset, size);
        List<RoleEntity> roleEntities = roleConverter.toDomainEntities(roleList);
        long total = roleDao.countRole(name);
        return PageResponse.ofList(offset / size + 1, size, total, roleEntities);
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
    public Optional<RoleEntity> findRoleById(Long roleId) {
        Role role = roleDao.selectRoleById(roleId);
        return Optional.ofNullable(roleConverter.toDomainEntity(role));
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
    public RoleEntity saveRole(RoleEntity roleEntity) {
        Role role = roleConverter.toDataObject(roleEntity);
        try {
            if (role.getId() == null) {
                roleDao.insertRole(role);
                roleEntity.setId(role.getId());
            } else {
                roleDao.updateRole(role);
            }
            return roleConverter.toDomainEntity(role);
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
    public void saveMenu(MenuEntity menuEntity) {
        Menu menu = menuConverter.toDataObject(menuEntity);
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
    public List<MenuEntity> findMenusByIds(List<Long> menuIds) {
        List<Menu> menuList = menuDao.listByIds(menuIds);
        return menuConverter.toDomainEntities(menuList);
    }

    @Override
    public List<String> findPermissionsByUserId(Long userId) {
        return menuDao.findPermissionsByUserid(userId);
    }
    
    @Override
    public List<String> findDirectPermissionsByUserId(Long userId) {
        return menuDao.findDirectPermissionsByUserid(userId);
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