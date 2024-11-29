package cn.xu.domain.permission.service.service;

import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.MenuOptionsEntity;
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
        List<MenuEntity> menuEntityList = permissionRepository.selectMenuList();
        // 2组装成树形结构
        List<MenuEntity> resultList = buildMenuTree(menuEntityList);

        // 3返回树形结构排序
        resultList.sort(Comparator.comparingInt(MenuEntity::getSort));

        // 4返回树形结构
        return resultList;
    }

    @Override
    public List<MenuOptionsEntity> getMenuOptionsTree() {
        // 1查询所有菜单
        List<MenuEntity> menuEntityList = permissionRepository.selectMenuList();

        // 2组装成树形结构
        List<MenuOptionsEntity> resultList = new ArrayList<>();
        for (MenuEntity menu : menuEntityList) {
            Long parentId = menu.getParentId();
            if ( parentId == null || parentId == 0) {
                resultList.add(MenuOptionsEntity.builder()
                        .label(menu.getTitle())
                        .value(menu.getId())
                        .build());
            }
        }
        for (MenuOptionsEntity menu : resultList) {
            menu.setChildren(getOptionsChild(menu.getValue(),menuEntityList));
        }
        return resultList;
    }

    @Override
    public MenuEntity selectMenuById(Long id) {
        return permissionRepository.selectMenuById(id);
    }

    /**
     * 构建树形结构
     * @return
     */
    public List<MenuEntity> buildMenuTree(List<MenuEntity> menuList) {
        // 创建一个Map以便快速查找
        Map<Long, MenuEntity> menuMap = menuList.stream()
                .collect(Collectors.toMap(MenuEntity::getId, menu -> menu));

        // 创建根节点列表
        List<MenuEntity> tree = new ArrayList<>();

        for (MenuEntity menu : menuList) {
            // 如果没有父节点，说明是根节点
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                tree.add(menu);
            } else {
                // 如果有父节点，将当前节点添加到父节点的children中
                MenuEntity parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }
        return tree;
    }

    /**
     * 分配角色权限-下拉菜单树
     * @param pid
     * @param menus
     * @return
     */
    private List<MenuOptionsEntity> getOptionsChild(Long pid , List<MenuEntity> menus){
        if (menus == null) {
            return Collections.emptyList();
        }

        Map<Long, MenuOptionsEntity> optionsMap = new HashMap<>();
        for (MenuEntity menu : menus) {
            Long parentId = menu.getParentId();
            if (parentId != null && parentId.equals(pid)) {
                optionsMap.put(menu.getId(), MenuOptionsEntity.builder()
                        .label(menu.getTitle())
                        .value(menu.getId())
                        .build());
            }
        }

        List<MenuOptionsEntity> children = new ArrayList<>(optionsMap.values());

        for (MenuOptionsEntity e : children) {
            e.setChildren(getOptionsChild(e.getValue(), menus));
        }

        return children.isEmpty() ? Collections.emptyList() : children;
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
