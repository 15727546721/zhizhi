package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {

    List<Menu> selectMenuList();

    Menu selectMenuById(@Param("id") Long id);

    List<Long> selectAllMenuId();

    void addMenu(Menu build);

    void updateMenu(Menu build);

    void deleteMenu(Long id);

    /**
     * 根据用户id获取用户的菜单id列表
     *
     * @param userId 用户id
     * @return 菜单id列表
     */
    List<Long> getMenuById(@Param("userId") long userId);

    /**
     * 根据菜单id列表获取菜单列表
     *
     * @param menuIds
     * @return
     */
    List<Menu> listByIds(@Param("menuIds") List<Long> menuIds);

    /**
     * 根据用户id获取用户的菜单权限
     *
     * @param userId 用户id
     * @return 菜单权限列表
     */
    List<String> findPermissionsByUserid(@Param("userId") Long userId);
    
    /**
     * 根据用户id获取用户直接关联的菜单权限
     *
     * @param userId 用户id
     * @return 直接菜单权限列表
     */
    List<String> findDirectPermissionsByUserid(@Param("userId") Long userId);
}
