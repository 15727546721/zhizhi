package cn.xu.domain.permission.service;

import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.MenuOptionsEntity;

import java.util.List;

public interface IPermissionService {

    /**
     * 获取菜单树列表
     *
     * @return
     */
    List<MenuEntity> selectMenuTreeList();

    /**
     * 获取菜单选项树
     * @return
     *
     */
    List<MenuOptionsEntity> getMenuOptionsTree();

    MenuEntity selectMenuById(Long id);

}
