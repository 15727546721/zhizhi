package cn.xu.domain.permission.service.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.dto.common.PageResponse;
import cn.xu.api.dto.permission.RoleAddOrUpdateRequest;
import cn.xu.api.dto.permission.RoleMenuRequest;
import cn.xu.common.Constants;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.MenuOptionsEntity;
import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.model.entity.RouterEntity;
import cn.xu.domain.permission.model.vo.MenuComponentVO;
import cn.xu.domain.permission.model.vo.MenuTypeVO;
import cn.xu.domain.permission.repository.IPermissionRepository;
import cn.xu.domain.permission.service.IPermissionService;
import cn.xu.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PermissionService implements IPermissionService {

    @Resource
    private IPermissionRepository permissionRepository;
    @Resource
    private TransactionTemplate transactionTemplate;

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
            if (parentId == null || parentId == 0) {
                resultList.add(MenuOptionsEntity.builder()
                        .label(menu.getTitle())
                        .id(menu.getId())
                        .build());
            }
        }
        for (MenuOptionsEntity menu : resultList) {
            menu.setChildren(getOptionsChild(menu.getId(), menuEntityList));
        }
        return resultList;
    }

    @Override
    public MenuEntity selectMenuById(Long id) {
        return permissionRepository.selectMenuById(id);
    }

    @Override
    public PageResponse<List<RoleEntity>> selectRolePage(String name, int page, int size) {
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 10;
        }
        List<RoleEntity> roleEntities = permissionRepository.selectRolePage(name, page, size);
        long total = permissionRepository.countRole(name);
        return PageResponse.<List<RoleEntity>>builder()
                .data(roleEntities)
                .total(total)
                .page(page)
                .size(size)
                .build();
    }

    @Override
    public List<Long> getCurrentUserRole() {
        long userId = StpUtil.getLoginIdAsLong();
        Long roleId = permissionRepository.selectRoleIdByUserId(userId);
        List<Long> menuIds = permissionRepository.selectMenuIdByRoleMenu(roleId);
        return menuIds;
    }

    @Override
    public List<Long> selectRoleMenuById(Long roleId) {
        RoleEntity roleEntity = permissionRepository.selectRoleById(roleId);
        if (roleEntity != null && roleEntity.getCode().equals("admin")) {
            return permissionRepository.selectAllMenuId();
        }
        List<Long> list = permissionRepository.selectMenuIdByRoleMenu(roleId);
        return list;
    }

    @Override
    public void assignRoleMenus(RoleMenuRequest roleMenuRequest) {
        Long roleId = roleMenuRequest.getRoleId();
        List<Long> menuIds = roleMenuRequest.getMenuIds();
        if (roleId != null && menuIds.isEmpty()) {
            // 将角色绑定的权限菜单清空
            permissionRepository.deleteRoleMenuByRoleId(roleId);
            return;
        }
        if (roleId == null || menuIds.isEmpty()) {
            throw new AppException(Constants.ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
        }
        RoleEntity roleEntity = permissionRepository.selectRoleById(roleId);
        if (roleEntity != null && roleEntity.getCode().equals("admin")) {
            return;
        }
        transactionTemplate.execute(status -> {
            try {
                // 先删除原有菜单权限
                permissionRepository.deleteRoleMenuByRoleId(roleId);
                // 再插入新菜单权限
                permissionRepository.insertRoleMenu(roleId, menuIds);
                return null;
            } catch (Exception e) {
                // 回滚事务
                status.setRollbackOnly();
                log.error("分配角色权限失败", e);
                throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "分配角色权限失败");
            }
        });
    }

    @Override
    public void addRole(RoleAddOrUpdateRequest role) {
        permissionRepository.addRole(RoleEntity.builder()
                .code(role.getCode())
                .name(role.getName())
                .desc(role.getDesc())
                .build());
    }

    @Override
    public void updateRole(RoleAddOrUpdateRequest role) {
        permissionRepository.updateRole(RoleEntity.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .desc(role.getDesc())
                .build());
    }

    @Override
    public void deleteRoleByIds(List<Long> ids) {
        permissionRepository.deleteRoleByIds(ids);
    }

    @Override
    public void addMenu(MenuEntity menu) {
        if (menu.getType().equals(MenuTypeVO.CATALOG.getCode())) {
            menu.setComponent(MenuComponentVO.Layout.getName());
        }
        permissionRepository.addMenu(menu);
    }

    @Override
    public void updateMenu(MenuEntity menu) {
        permissionRepository.updateMenu(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        permissionRepository.deleteMenu(id);
    }

    @Override
    public List<RouterEntity> getCurrentUserMenu() {
        List<MenuEntity> menus;
        try {
            if (StpUtil.hasRole("admin")) {
                menus = permissionRepository.selectMenuList();
            } else {
                List<Long> menuIds = permissionRepository.getMenuById(StpUtil.getLoginIdAsLong());
                menus = permissionRepository.listByIds(menuIds);
            }
        } catch (Exception e) {
            log.error("获取当前用户菜单失败", e);
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "获取当前用户菜单失败");
        }

        List<RouterEntity> routerList = buildRouterTree(menus);
        return routerList;
    }

    /**
     * 构建树形结构
     *
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
     *
     * @param pid
     * @param menus
     * @return
     */
    private List<MenuOptionsEntity> getOptionsChild(Long pid, List<MenuEntity> menus) {
        if (menus == null) {
            return Collections.emptyList();
        }

        Map<Long, MenuOptionsEntity> optionsMap = new HashMap<>();
        for (MenuEntity menu : menus) {
            Long parentId = menu.getParentId();
            if (parentId != null && parentId == pid) {
                optionsMap.put(menu.getId(), MenuOptionsEntity.builder()
                        .label(menu.getTitle())
                        .id(menu.getId())
                        .build());
            }
        }

        List<MenuOptionsEntity> children = new ArrayList<>(optionsMap.values());

        for (MenuOptionsEntity child : children) {
            child.setChildren(getOptionsChild(child.getId(), menus));
        }

        return children.isEmpty() ? Collections.emptyList() : children;
    }

    /**
     * 构建路由树
     *
     * @param menus
     * @return
     */
    private List<RouterEntity> buildRouterTree(List<MenuEntity> menus) {
        List<RouterEntity> resultList = new ArrayList<>();
        for (MenuEntity menu : menus) {
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0) {
                RouterEntity.MetaEntity metaVO = new RouterEntity.MetaEntity(menu.getTitle(), menu.getIcon(), menu.getHidden());
                RouterEntity build = RouterEntity.builder().id(menu.getId()).path(menu.getPath()).redirect(menu.getRedirect()).name(menu.getName()).component(menu.getComponent())
                        .meta(metaVO).sort(menu.getSort()).build();
                resultList.add(build);
            }
        }
        resultList.sort(Comparator.comparingInt(RouterEntity::getSort));

        for (RouterEntity RouterEntity : resultList) {
            RouterEntity.setChildren(getRouterChild(RouterEntity.getId(), menus));
        }
        return resultList;
    }

    /**
     * 获取路由菜单树
     *
     * @param pid
     * @param menus
     * @return
     */
    private List<RouterEntity> getRouterChild(Long pid, List<MenuEntity> menus) {
        if (menus == null) {
            return Collections.emptyList();
        }
        Map<Long, RouterEntity> routerMap = new HashMap<>();
        for (MenuEntity e : menus) {
            Long parentId = e.getParentId();
            if (parentId != null && parentId.equals(pid)) {
                // 子菜单的下级菜单
                RouterEntity.MetaEntity metaVO = new RouterEntity.MetaEntity(e.getTitle(), e.getIcon(), e.getHidden());
                RouterEntity build = RouterEntity.builder().id(e.getId()).path(e.getPath()).redirect(e.getRedirect()).name(e.getName()).component(e.getComponent())
                        .meta(metaVO).sort(e.getSort()).build();
                routerMap.put(e.getId(), build);
            }
        }

        List<RouterEntity> children = new ArrayList<>(routerMap.values());
        children.sort(Comparator.comparingInt(RouterEntity::getSort));

        for (RouterEntity e : children) {
            e.setChildren(getRouterChild(e.getId(), menus));
        }

        return children.isEmpty() ? Collections.emptyList() : children;
    }
}
