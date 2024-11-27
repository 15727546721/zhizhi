package cn.xu.domain.permission.repository;

import cn.xu.infrastructure.persistent.po.Menu;

import java.util.List;

public interface IPermissionRepository {

    List<String> findRolesByUserid(Long userid);

    List<Menu> selectMenuList();
}
