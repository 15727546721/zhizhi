package cn.xu.domain.permission.repository;

import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.RoleEntity;

import java.util.List;

public interface IPermissionRepository {

    List<String> findRolesByUserid(Long userid);

    List<MenuEntity> selectMenuList();

    MenuEntity selectMenuById(Long id);

    /**
     * 分页查询角色列表
     *
     * @param name   角色名称（模糊查询）
     * @param offset 偏移量
     * @param size   每页数量
     * @return 角色列表
     */
    List<RoleEntity> selectRolePage(String name, int offset, int size);

    /**
     * 查询角色总数
     *
     * @param name 角色名称（模糊查询）
     * @return 总数
     */
    long countRole(String name);

    Long selectRoleIdByUserId(long userId);

    List<Long> selectMenuIdByRoleMenu(Long roleId);

    RoleEntity selectRoleById(Long roleId);

    List<Long> selectAllMenuId();

    void deleteRoleMenuByRoleId(Long roleId);

    void insertRoleMenu(Long roleId, List<Long> menuIds);

    void addRole(RoleEntity roleEntity);

    void updateRole(RoleEntity roleEntity);

    void deleteRoleByIds(List<Long> ids);

    void addMenu(MenuEntity build);

    void updateMenu(MenuEntity menu);

    void deleteMenu(Long id);

    List<Long> getMenuById(long userId);

    List<MenuEntity> listByIds(List<Long> menuIds);

    /**
     * 根据用户ID获取用户的权限列表
     *
     * @param userId
     * @return
     */
    List<String> findPermissionsByUserid(Long userId);

    /**
     * 检查角色编码是否存在
     *
     * @param code 角色编码
     * @return 是否存在
     */
    boolean existsByCode(String code);
}
