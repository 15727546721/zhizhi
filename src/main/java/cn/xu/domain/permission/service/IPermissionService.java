package cn.xu.domain.permission.service;

import cn.xu.api.dto.common.PageResponse;
import cn.xu.api.dto.permission.RoleAddOrUpdateRequest;
import cn.xu.api.dto.permission.RoleMenuRequest;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.MenuOptionsEntity;
import cn.xu.domain.permission.model.entity.RouterEntity;

import java.util.List;

public interface IPermissionService {

    /**
     * 获取菜单树列表
     *
     * @return
     */
    List<MenuEntity> selectMenuTreeList();

    /**
     * 获取菜单选项树
     * @return
     *
     */
    List<MenuOptionsEntity> getMenuOptionsTree();

    MenuEntity selectMenuById(Long id);

    PageResponse selectRolePage(String name, int page, int size);

    List<Long> getCurrentUserRole();

    List<Long> selectRoleMenuById(Long roleId);

    void assignRoleMenus(RoleMenuRequest roleMenuRequest);

    void addRole(RoleAddOrUpdateRequest role);

    void updateRole(RoleAddOrUpdateRequest role);

    void deleteRoleByIds(List<Long> ids);

    void addMenu(MenuEntity menu);

    void updateMenu(MenuEntity menu);

    void deleteMenu(Long id);

    List<RouterEntity> getCurrentUserMenu();
}
