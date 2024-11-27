package cn.xu.domain.permission.service;

import cn.xu.common.ResponseEntity;
import cn.xu.domain.permission.model.entity.MenuEntity;

import java.util.List;

public interface IPermissionService {

    /**
     * 获取菜单树列表
     *
     * @return
     */
    List<MenuEntity> selectMenuTreeList();
}
