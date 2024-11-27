package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMenuDao {

    List<Menu> selectMenuList();

    Menu selectMenuById(@Param("id") Long id);
}




