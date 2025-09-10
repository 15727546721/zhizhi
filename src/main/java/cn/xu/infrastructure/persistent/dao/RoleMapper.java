package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper接口
 * 
 * @author Lily
 */
@Mapper
public interface RoleMapper {
    /**
     * 根据用户ID查询角色列表
     */
    List<String> selectRolesByUserid(@Param("userId") Long userId);

    /**
     * 分页查询角色列表
     */
    List<Role> selectRolePage(@Param("name") String name, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 查询角色总数
     */
    long countRole(@Param("name") String name);

    /**
     * 根据用户ID查询角色ID
     */
    Long selectRoleIdByUserId(@Param("userId") long userId);

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> selectMenuIdByRoleMenu(@Param("roleId") Long roleId);

    /**
     * 根据角色ID查询角色信息
     */
    Role selectRoleById(@Param("roleId") Long roleId);

    /**
     * 删除角色的菜单权限
     */
    void deleteRoleMenuByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色的菜单权限
     */
    void insertRoleMenu(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    /**
     * 新增角色
     */
    void insertRole(Role role);

    /**
     * 更新角色
     */
    void updateRole(Role role);

    /**
     * 批量删除角色
     */
    void deleteRoleByIds(@Param("ids") List<Long> ids);

    /**
     * 批量删除角色的菜单权限
     */
    void deleteRoleMenuByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据编码查询数量
     */
    int countByCode(@Param("code") String code);
    
    /**
     * 根据用户ID查询角色列表
     */
    List<Role> findRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询关联的用户ID列表
     */
    List<Long> findUserIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色编码查询角色
     */
    Role selectRoleByCode(@Param("code") String code);
}