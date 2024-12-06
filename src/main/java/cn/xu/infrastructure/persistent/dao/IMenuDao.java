package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMenuDao {

    List<Menu> selectMenuList();

    Menu selectMenuById(@Param("id") Long id);

    List<Long> selectAllMenuId();

    void addMenu(Menu build);

    void updateMenu(Menu build);

    void deleteMenu(Long id);

    List<Long> getMenuById(@Param("userId") long userId);

    List<Menu> listByIds(@Param("menuIds") List<Long> menuIds);
}




