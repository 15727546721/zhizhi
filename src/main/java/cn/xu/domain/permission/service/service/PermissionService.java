package cn.xu.domain.permission.service.service;

import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.repository.IPermissionRepository;
import cn.xu.domain.permission.service.IPermissionService;
import cn.xu.infrastructure.persistent.po.Menu;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService implements IPermissionService {

    @Resource
    private IPermissionRepository permissionRepository;

    @Override
    public List<MenuEntity> selectMenuTreeList() {
        // 1查询所有菜单
        List<Menu> menuList = permissionRepository.selectMenuList();
        // 2组装成树形结构
        // 转成menuEntity
        List<MenuEntity> menuEntityList = menuList.stream()
                .map(this::convert)
                .collect(Collectors.toList());

        // 使用Map来优化查找性能，这里应该使用sort作为键
        Map<Integer, List<MenuEntity>> menuMap = menuEntityList.stream()
                .collect(Collectors.groupingBy(MenuEntity::getSort));

        // 构建树形结构
        List<MenuEntity> resultList = new ArrayList<>();
        for (MenuEntity menu : menuEntityList) {
            Long parentId = menu.getParentId();
            if ( parentId == null || parentId == 0)
                resultList.add(menu);
        }
        for (MenuEntity menu : resultList) {
            menu.setChildren(getMenTreeChild(menu.getId(), menuEntityList));
        }
        resultList.sort(Comparator.comparingInt(MenuEntity::getSort));

        // 3返回树形结构
        return resultList;
    }

    /**
     * 递归构建树形结构
     * @return
     */
    private List<MenuEntity> getMenTreeChild(Long pid , List<MenuEntity> menus){
        List<MenuEntity> children = new ArrayList<>();
        for (MenuEntity e: menus) {
            Long parentId = e.getParentId();
            if(parentId != null && parentId.equals(pid)){
                // 子菜单的下级菜单
                children.add( e );
            }
        }
        // 把子菜单的子菜单再循环一遍
        for (MenuEntity e: children) {
            // children
            e.setChildren(getMenTreeChild(e.getId(),menus));
        }
        //停下来的条件，如果 没有子元素了，则停下来
        if(children.isEmpty()){
            return null;
        }
        return children;
    }




    private MenuEntity convert(Menu menu) {
        if (menu == null) {
            return null;
        }
        return MenuEntity.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .path(menu.getPath())
                .component(menu.getComponent())
                .title(menu.getTitle())
                .sort(menu.getSort())
                .icon(menu.getIcon())
                .type(menu.getType())
                .createdTime(menu.getCreatedTime())
                .updateTime(menu.getUpdateTime())
                .redirect(menu.getRedirect())
                .name(menu.getName())
                .hidden(menu.getHidden())
                .perm(menu.getPerm())
                .children(new ArrayList<>()) // 初始化子菜单列表
                .build();
    }
}
