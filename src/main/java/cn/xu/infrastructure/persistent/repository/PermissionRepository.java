package cn.xu.infrastructure.persistent.repository;


import cn.xu.application.common.ResponseCode;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.repository.IPermissionRepository;
import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.persistent.dao.IMenuDao;
import cn.xu.infrastructure.persistent.dao.IRoleDao;
import cn.xu.infrastructure.persistent.po.Menu;
import cn.xu.infrastructure.persistent.po.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PermissionRepository implements IPermissionRepository {

    @Resource
    private IRoleDao roleDao;
    @Resource
    private IMenuDao menuDao;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public List<String> findRolesByUserid(Long userid) {
        List<String> roles = roleDao.selectRolesByUserid(userid);
        return roles;
    }

    @Override
    public List<MenuEntity> selectMenuList() {
        List<Menu> menuList = menuDao.selectMenuList();
        List<MenuEntity> menuEntityList = menuList.stream()
                .map(this::convert)
                .collect(Collectors.toList());
        return menuEntityList;
    }

    @Override
    public MenuEntity selectMenuById(Long id) {
        Menu menu = menuDao.selectMenuById(id);
        return convert(menu);
    }

    @Override
    public List<RoleEntity> selectRolePage(String name, int page, int size) {
        int offset = Math.max(0, (page - 1) * size);

        List<Role> roleList = roleDao.selectRolePage(name, offset, size);
        return roleList.stream()
                .map(this::convert)
                .collect(Collectors.toList());
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
        return convert(role);
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
        Role role = Role.builder()
                .name(roleEntity.getName())
                .code(roleEntity.getCode())
                .remark(roleEntity.getRemark())
                .build();
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
            roleDao.updateRole(Role.builder()
                    .id(roleEntity.getId())
                    .name(roleEntity.getName())
                    .code(roleEntity.getCode())
                    .remark(roleEntity.getRemark())
                    .build());
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
    public void addMenu(MenuEntity build) {
        menuDao.addMenu(Menu.builder()
                .parentId(build.getParentId())
                .path(build.getPath())
                .component(build.getComponent())
                .title(build.getTitle())
                .sort(build.getSort())
                .icon(build.getIcon())
                .type(build.getType())
                .redirect(build.getRedirect())
                .name(build.getName())
                .hidden(build.getHidden())
                .perm(build.getPerm())
                .build());
    }

    @Override
    public void updateMenu(MenuEntity menu) {
        menuDao.updateMenu(Menu.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .path(menu.getPath())
                .component(menu.getComponent())
                .title(menu.getTitle())
                .sort(menu.getSort())
                .icon(menu.getIcon())
                .type(menu.getType())
                .redirect(menu.getRedirect())
                .name(menu.getName())
                .hidden(menu.getHidden())
                .perm(menu.getPerm())
                .build());
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
        List<MenuEntity> menuEntityList = menuList.stream()
                .map(this::convert)
                .collect(Collectors.toList());
        return menuEntityList;
    }

    @Override
    public List<String> findPermissionsByUserid(Long userId) {
        return menuDao.findPermissionsByUserid(userId);
    }

    @Override
    public boolean existsByCode(String code) {
        return roleDao.countByCode(code) > 0;
    }

    private MenuEntity convert(Menu menu) {
        if (menu == null) {
            return null;
        }
        return MenuEntity.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .path(menu.getPath())
                .component(menu.getComponent())
                .title(menu.getTitle())
                .sort(menu.getSort())
                .icon(menu.getIcon())
                .type(menu.getType())
                .createTime(menu.getCreateTime())
                .updateTime(menu.getUpdateTime())
                .redirect(menu.getRedirect())
                .name(menu.getName())
                .hidden(menu.getHidden())
                .perm(menu.getPerm())
                .children(new ArrayList<>()) // 初始化子菜单列表
                .build();
    }

    private RoleEntity convert(Role role) {
        if (role == null) {
            return null;
        }
        return RoleEntity.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .remark(role.getRemark())
                .createTime(role.getCreateTime())
                .updateTime(role.getUpdateTime())
                .build();
    }
}
