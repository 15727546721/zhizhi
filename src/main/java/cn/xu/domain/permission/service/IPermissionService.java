package cn.xu.domain.permission.service;

import cn.xu.api.dto.common.PageResponse;
import cn.xu.api.dto.permission.RoleAddOrUpdateRequest;
import cn.xu.api.dto.permission.RoleMenuRequest;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.MenuOptionsEntity;
import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.model.entity.RouterEntity;

import java.util.List;

public interface IPermissionService {
    /**
     * 获取菜单树列表
     */
    List<MenuEntity> selectMenuTreeList();

    /**
     * 获取菜单选项树
     */
    List<MenuOptionsEntity> getMenuOptionsTree();

    /**
     * 根据ID查询菜单
     */
    MenuEntity selectMenuById(Long id);

    /**
     * 分页查询角色列表
     */
    PageResponse<List<RoleEntity>> selectRolePage(String name, int page, int size);

    /**
     * 查询角色总数
     */
    long countRole(String name);

    /**
     * 获取当前用户角色
     */
    List<Long> getCurrentUserRole();

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> selectRoleMenuById(Long roleId);

    /**
     * 分配角色菜单权限
     */
    void assignRoleMenus(RoleMenuRequest roleMenuRequest);

    /**
     * 添加角色
     */
    void addRole(RoleAddOrUpdateRequest role);

    /**
     * 更新角色
     */
    void updateRole(RoleAddOrUpdateRequest role);

    /**
     * 删除角色
     */
    void deleteRoleByIds(List<Long> ids);

    /**
     * 添加菜单
     */
    void addMenu(MenuEntity menu);

    /**
     * 更新菜单
     */
    void updateMenu(MenuEntity menu);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);

    /**
     * 获取当前用户菜单
     */
    List<RouterEntity> getCurrentUserMenu();
}
