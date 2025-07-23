package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.UserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleMapper {
    int insert(UserRole userRole);

    int deleteUserRoleByUserId(Long userId);
}
