package cn.xu.domain.permission.service;

import cn.xu.api.dto.common.PageResponse;
import cn.xu.api.dto.permission.RoleMenuRequest;
import cn.xu.api.dto.permission.RoleRequest;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.MenuOptionsEntity;

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

    void addRole(RoleRequest role);
}
