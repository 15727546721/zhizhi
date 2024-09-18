package cn.xu.infrastructure.persistent.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IRoleDao {
    List<String> selectRolesByUserid(@Param("userid") Long userid);
}
