package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单Mapper接口
 * 负责菜单相关的数据库操作
 */
@Mapper
public interface MenuMapper {

    /**
     * 查询所有菜单列表
     */
    List<Menu> selectMenuList();

    /**
     * 根据ID查询菜单
     */
    Menu selectMenuById(@Param("id") Long id);

    /**
     * 查询所有菜单ID
     */
    List<Long> selectAllMenuId();

    /**
     * 添加菜单
     */
    void addMenu(Menu build);

    /**
     * 更新菜单
     */
    void updateMenu(Menu build);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);

    /**
     * 根据用户ID获取用户的菜单ID列表
     *
     * @param userId 用户ID
     * @return 菜单ID列表
     */
    List<Long> getMenuById(@Param("userId") long userId);

    /**
     * 根据菜单ID列表获取菜单列表
     *
     * @param menuIds 菜单ID列表
     * @return 菜单列表
     */
    List<Menu> listByIds(@Param("menuIds") List<Long> menuIds);

    /**
     * 根据用户ID获取用户的菜单权限
     *
     * @param userId 用户ID
     * @return 菜单权限列表
     */
    List<String> findPermissionsByUserid(@Param("userId") Long userId);
    
    /**
     * 根据用户ID获取用户直接关联的菜单权限
     *
     * @param userId 用户ID
     * @return 直接菜单权限列表
     */
    List<String> findDirectPermissionsByUserid(@Param("userId") Long userId);
}