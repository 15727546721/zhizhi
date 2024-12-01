package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.infrastructure.persistent.po.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IRoleDao {
    List<String> selectRolesByUserid(@Param("userId") Long userId);

    List<Role> selectRolePage(@Param("name") String name, @Param("page") int page, @Param("size") int size);

    long countRole(@Param("name") String name);

    Long selectRoleIdByUserId(@Param("userId") long userId);

    List<Long> selectMenuIdByRoleMenu(@Param("roleId") Long roleId);

    Role selectRoleById(@Param("roleId") Long roleId);

    void deleteRoleMenuByRoleId(@Param("roleId") Long roleId);

    void insertRoleMenu(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}
