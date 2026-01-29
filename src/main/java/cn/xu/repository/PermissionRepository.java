package cn.xu.repository;

import cn.xu.common.response.PageResponse;
import cn.xu.model.entity.Menu;
import cn.xu.model.entity.Role;

import java.util.List;
import java.util.Optional;

/**
 * 权限仓储接口
 * <p>定义权限数据的持久化操作</p>
 
 */
public interface PermissionRepository {

    /**
     * 根据用户ID查询角色编码列表
     */
    List<String> findRolesByUserId(Long userId);

    /**
     * 查询所有菜单列表
     */
    List<Menu> findAllMenus();

    /**
     * 根据ID查询菜单
     */
    Optional<Menu> findMenuById(Long id);

    /**
     * 分页查询角色列表
     */
    PageResponse<List<Role>> findRolePage(String name, int offset, int size);

    /**
     * 根据用户ID查询角色ID
     */
    Optional<Long> findRoleIdByUserId(long userId);

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> findMenuIdsByRoleId(Long roleId);

    /**
     * 根据角色ID查询角色信息
     */
    Optional<Role> findRoleById(Long roleId);

    /**
     * 查询所有菜单ID
     */
    List<Long> findAllMenuIds();

    /**
     * 删除角色的菜单权限
     */
    void deleteRoleMenuByRoleId(Long roleId);

    /**
     * 批量插入角色的菜单权限
     */
    void insertRoleMenu(Long roleId, List<Long> menuIds);

    /**
     * 保存角色
     */
    Role saveRole(Role roleEntity);

    /**
     * 删除角色
     */
    void deleteRoleByIds(List<Long> ids);

    /**
     * 保存菜单
     */
    void saveMenu(Menu menuEntity);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);

    /**
     * 根据用户ID获取用户的菜单ID列表
     */
    List<Long> findMenuIdsByUserId(long userId);

    /**
     * 根据菜单ID列表获取菜单列表
     */
    List<Menu> findMenusByIds(List<Long> menuIds);

    /**
     * 根据用户ID获取用户的权限列表
     */
    List<String> findPermissionsByUserId(Long userId);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 根据角色ID查询关联的用户ID列表
     */
    List<Long> findUserIdsByRoleId(Long roleId);
}
