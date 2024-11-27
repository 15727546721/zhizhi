package cn.xu.domain.permission.repository;

import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.infrastructure.persistent.po.Menu;

import java.util.List;

public interface IPermissionRepository {

    List<String> findRolesByUserid(Long userid);

    List<MenuEntity> selectMenuList();

    MenuEntity selectMenuById(Long id);
}
