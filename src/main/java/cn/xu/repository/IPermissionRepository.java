package cn.xu.repository;

import cn.xu.common.response.PageResponse;
import cn.xu.model.entity.Menu;
import cn.xu.model.entity.Role;

import java.util.List;
import java.util.Optional;

/**
 * 权限仓储接口
 * 遵循DDD规范，定义领域层仓储接口
 * 
 * 
 */
public interface IPermissionRepository {

    /**
     * 根据用户ID查询角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> findRolesByUserId(Long userId);

    /**
     * 查询所有菜单列表
     *
     * @return 菜单列表
     */
    List<Menu> findAllMenus();

    /**
     * 根据ID查询菜单
     *
     * @param id 菜单ID
     * @return 菜单实体
     */
    Optional<Menu> findMenuById(Long id);

    /**
     * 分页查询角色列表
     *
     * @param name   角色名称（模糊查询）
     * @param offset 偏移量
     * @param size   每页数量
     * @return 角色分页结果
     */
    PageResponse<List<Role>> findRolePage(String name, int offset, int size);

    /**
     * 根据用户ID查询角色ID
     *
     * @param userId 用户ID
     * @return 角色ID
     */
    Optional<Long> findRoleIdByUserId(long userId);

    /**
     * 根据角色ID查询菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> findMenuIdsByRoleId(Long roleId);

    /**
     * 根据角色ID查询角色信息
     *
     * @param roleId 角色ID
     * @return 角色实体
     */
    Optional<Role> findRoleById(Long roleId);

    /**
     * 查询所有菜单ID
     *
     * @return 菜单ID列表
     */
    List<Long> findAllMenuIds();

    /**
     * 删除角色的菜单权限
     *
     * @param roleId 角色ID
     */
    void deleteRoleMenuByRoleId(Long roleId);

    /**
     * 批量插入角色的菜单权限
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    void insertRoleMenu(Long roleId, List<Long> menuIds);

    /**
     * 保存角色
     *
     * @param roleEntity 角色实体
     * @return 保存后的角色实体
     */
    Role saveRole(Role roleEntity);

    /**
     * 删除角色
     *
     * @param ids 角色ID列表
     */
    void deleteRoleByIds(List<Long> ids);

    /**
     * 保存菜单
     *
     * @param menuEntity 菜单实体
     */
    void saveMenu(Menu menuEntity);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    void deleteMenu(Long id);

    /**
     * 根据用户ID获取用户的菜单ID列表
     *
     * @param userId 用户ID
     * @return 菜单ID列表
     */
    List<Long> findMenuIdsByUserId(long userId);

    /**
     * 根据菜单ID列表获取菜单列表
     *
     * @param menuIds 菜单ID列表
     * @return 菜单列表
     */
    List<Menu> findMenusByIds(List<Long> menuIds);

    /**
     * 根据用户ID获取用户的权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> findPermissionsByUserId(Long userId);

    /**
     * 检查角色编码是否存在
     *
     * @param code 角色编码
     * @return 是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 根据角色ID查询关联的用户ID列表
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> findUserIdsByRoleId(Long roleId);
}