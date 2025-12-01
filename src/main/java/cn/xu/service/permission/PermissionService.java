package cn.xu.service.permission;

import cn.xu.common.response.PageResponse;
import cn.xu.model.dto.permission.RoleAddOrUpdateRequest;
import cn.xu.model.dto.permission.RoleMenuRequest;
import cn.xu.model.entity.Menu;
import cn.xu.model.entity.Role;
import cn.xu.model.vo.permission.MenuOptionsVO;
import cn.xu.model.vo.permission.RouterVO;

import java.util.List;
import java.util.Optional;

/**
 * 权限服务接口
 * 定义权限相关的业务操作
 */
public interface PermissionService {
    /**
     * 获取菜单树列表
     *
     * @return 菜单树列表
     */
    List<Menu> getMenuTreeList();

    /**
     * 获取菜单选项树
     *
     * @return 菜单选项树
     */
    List<MenuOptionsVO> getMenuOptionsTree();

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
     * @param name  角色名称（模糊查询）
     * @param page  页码
     * @param size  每页数量
     * @return 角色分页结果
     */
    PageResponse<List<Role>> findRolePage(String name, int page, int size);

    /**
     * 获取当前用户角色的菜单ID列表
     *
     * @return 菜单ID列表
     */
    List<Long> getCurrentUserRoleMenuIds();

    /**
     * 根据角色ID查询菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> findRoleMenuIdsById(Long roleId);

    /**
     * 分配角色菜单权限
     *
     * @param roleMenuRequest 角色菜单请求
     */
    void assignRoleMenus(RoleMenuRequest roleMenuRequest);

    /**
     * 添加角色
     *
     * @param role 角色添加或更新请求
     */
    void addRole(RoleAddOrUpdateRequest role);

    /**
     * 更新角色
     *
     * @param role 角色添加或更新请求
     */
    void updateRole(RoleAddOrUpdateRequest role);

    /**
     * 删除角色
     *
     * @param ids 角色ID列表
     */
    void deleteRoleByIds(List<Long> ids);

    /**
     * 添加菜单
     *
     * @param menu 菜单实体
     */
    void addMenu(Menu menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单实体
     */
    void updateMenu(Menu menu);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    void deleteMenu(Long id);

    /**
     * 获取当前用户菜单
     *
     * @return 路由实体列表
     */
    List<RouterVO> getCurrentUserMenu();
}
