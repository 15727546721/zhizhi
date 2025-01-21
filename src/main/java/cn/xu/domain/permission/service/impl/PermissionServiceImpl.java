package cn.xu.domain.permission.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.permission.RoleAddOrUpdateRequest;
import cn.xu.api.web.model.dto.permission.RoleMenuRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.MenuOptionsEntity;
import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.model.entity.RouterEntity;
import cn.xu.domain.permission.model.vo.MenuComponentVO;
import cn.xu.domain.permission.model.vo.MenuTypeVO;
import cn.xu.domain.permission.repository.IPermissionRepository;
import cn.xu.domain.permission.service.IPermissionService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PermissionServiceImpl implements IPermissionService {

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
        // 参数校验和默认值设置
        page = Math.max(1, page);  // 确保页码最小为1
        size = size <= 0 ? 10 : Math.min(size, 100);  // 确保每页数量在1-100之间

        // 查询数据
        List<RoleEntity> roleEntities = permissionRepository.selectRolePage(name, page, size);
        long total = permissionRepository.countRole(name);

        return PageResponse.of(page, size, total, roleEntities);
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
        // 1. 检查角色是否存在
        RoleEntity roleEntity = permissionRepository.selectRoleById(roleId);
        if (roleEntity == null) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "角色不存在");
        }

        // 2. 如果是超级管理员角色，返回所有菜单ID
        if ("admin".equals(roleEntity.getCode())) {
            return permissionRepository.selectAllMenuId();
        }

        // 3. 获取角色的菜单权限
        List<Long> menuIds = permissionRepository.selectMenuIdByRoleMenu(roleId);
        if (menuIds == null) {
            menuIds = Collections.emptyList();
        }

        return menuIds;
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
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
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
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "分配角色权限失败");
            }
        });
    }

    @Override
    public void addRole(RoleAddOrUpdateRequest role) {
        // 1. 检查角色编码是否已存在
        if (permissionRepository.existsByCode(role.getCode())) {
            throw new BusinessException(ResponseCode.DUPLICATE_KEY.getCode(), "角色编码已存在");
        }

        // 2. 创建角色实体
        RoleEntity roleEntity = RoleEntity.builder()
                .code(role.getCode())
                .name(role.getName())
                .remark(role.getRemark())
                .build();

        // 3. 保存角色
        permissionRepository.addRole(roleEntity);
    }

    @Override
    public void updateRole(RoleAddOrUpdateRequest role) {
        // 1. 检查角色是否存在
        RoleEntity existingRole = permissionRepository.selectRoleById(role.getId());
        if (existingRole == null) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "角色不存在");
        }

        // 2. 如果修改了编码，检查新编码是否已存在
        if (!existingRole.getCode().equals(role.getCode()) &&
                permissionRepository.existsByCode(role.getCode())) {
            throw new BusinessException(ResponseCode.DUPLICATE_KEY.getCode(), "角色编码已存在");
        }

        // 3. 更新角色
        RoleEntity roleEntity = RoleEntity.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .remark(role.getRemark())
                .build();

        permissionRepository.updateRole(roleEntity);
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
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取当前用户菜单失败");
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
     */
    private List<RouterEntity> buildRouterTree(List<MenuEntity> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        // 使用Stream API过滤和转换根节点
        List<RouterEntity> rootRouters = menus.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .map(this::convertMenuToRouter)
                .sorted(Comparator.comparingInt(RouterEntity::getSort))
                .collect(Collectors.toList());

        // 为每个根节点设置子节点
        rootRouters.forEach(router -> router.setChildren(getRouterChild(router.getId(), menus)));

        return rootRouters;
    }

    /**
     * 获取路由菜单树的子节点
     */
    private List<RouterEntity> getRouterChild(Long parentId, List<MenuEntity> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        // 使用Stream API过滤和转换子节点
        List<RouterEntity> children = menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .map(this::convertMenuToRouter)
                .sorted(Comparator.comparingInt(RouterEntity::getSort))
                .collect(Collectors.toList());

        // 递归设置子节点的子节点
        children.forEach(child -> child.setChildren(getRouterChild(child.getId(), menus)));

        return children;
    }

    /**
     * 将菜单实体转换为路由实体
     */
    private RouterEntity convertMenuToRouter(MenuEntity menu) {
        RouterEntity.MetaEntity meta = new RouterEntity.MetaEntity(
                menu.getTitle(),
                menu.getIcon(),
                menu.getHidden()
        );

        return RouterEntity.builder()
                .id(menu.getId())
                .path(menu.getPath())
                .redirect(menu.getRedirect())
                .name(menu.getName())
                .component(menu.getComponent())
                .meta(meta)
                .sort(menu.getSort())
                .build();
    }

    @Override
    public long countRole(String name) {
        return permissionRepository.countRole(name);
    }
}
