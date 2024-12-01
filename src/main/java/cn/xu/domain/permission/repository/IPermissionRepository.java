package cn.xu.domain.permission.repository;

import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.infrastructure.persistent.po.Menu;
import cn.xu.infrastructure.persistent.po.Role;

import java.util.List;

public interface IPermissionRepository {

    List<String> findRolesByUserid(Long userid);

    List<MenuEntity> selectMenuList();

    MenuEntity selectMenuById(Long id);

    List<RoleEntity> selectRolePage(String name, int page, int size);

    long countRole(String name);

    Long selectRoleIdByUserId(long userId);

    List<Long> selectMenuIdByRoleMenu(Long roleId);

    RoleEntity selectRoleById(Long roleId);

    List<Long> selectAllMenuId();

    void deleteRoleMenuByRoleId(Long roleId);

    void insertRoleMenu(Long roleId, List<Long> menuIds);
}
