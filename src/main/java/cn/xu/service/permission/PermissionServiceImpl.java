package cn.xu.service.permission;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.response.PageResponse;
import cn.xu.config.satoken.UserPermission;
import cn.xu.model.dto.permission.RoleAddOrUpdateRequest;
import cn.xu.model.dto.permission.RoleMenuRequest;
import cn.xu.model.entity.Menu;
import cn.xu.model.entity.Role;
import cn.xu.model.vo.permission.MenuComponentVO;
import cn.xu.model.vo.permission.MenuOptionsVO;
import cn.xu.model.vo.permission.MenuTypeVO;
import cn.xu.model.vo.permission.RouterVO;
import cn.xu.repository.IPermissionRepository;
import cn.xu.support.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 * 
 * @author xu
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private IPermissionRepository permissionRepository;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private UserPermission userPermission;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Menu> getMenuTreeList() {
        List<Menu> menuEntityList = permissionRepository.findAllMenus();
        List<Menu> resultList = buildMenuTree(menuEntityList);
        resultList.sort(Comparator.comparingInt(Menu::getSort));
        return resultList;
    }

    @Override
    public List<MenuOptionsVO> getMenuOptionsTree() {
        List<Menu> menuEntityList = permissionRepository.findAllMenus();
        return menuEntityList.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .map(menu -> MenuOptionsVO.builder()
                        .label(menu.getTitle())
                        .id(menu.getId())
                        .children(getOptionsChild(menu.getId(), menuEntityList))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Menu> findMenuById(Long id) {
        return permissionRepository.findMenuById(id);
    }

    @Override
    public PageResponse<List<Role>> findRolePage(String name, int page, int size) {
        page = Math.max(1, page);
        size = size <= 0 ? 10 : Math.min(size, 100);
        int offset = (page - 1) * size;
        return permissionRepository.findRolePage(name, offset, size);
    }

    @Override
    public List<Long> getCurrentUserRoleMenuIds() {
        long userId = StpUtil.getLoginIdAsLong();
        return permissionRepository.findMenuIdsByUserId(userId);
    }

    @Override
    public List<Long> findRoleMenuIdsById(Long roleId) {
        Optional<Role> roleEntityOptional = permissionRepository.findRoleById(roleId);
        if (!roleEntityOptional.isPresent()) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "角色不存在");
        }

        Role roleEntity = roleEntityOptional.get();
        if ("admin".equals(roleEntity.getCode())) {
            return permissionRepository.findAllMenuIds();
        }

        List<Long> menuIds = permissionRepository.findMenuIdsByRoleId(roleId);
        return menuIds != null ? menuIds : Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenus(RoleMenuRequest roleMenuRequest) {
        Long roleId = roleMenuRequest.getRoleId();
        List<Long> menuIds = roleMenuRequest.getMenuIds();
        
        if (roleId != null && menuIds.isEmpty()) {
            permissionRepository.deleteRoleMenuByRoleId(roleId);
            clearUserPermissionsCacheByRoleId(roleId);
            return;
        }
        if (roleId == null || menuIds.isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
        }
        
        Optional<Role> roleEntityOptional = permissionRepository.findRoleById(roleId);
        if (roleEntityOptional.isPresent() && "admin".equals(roleEntityOptional.get().getCode())) {
            return;
        }
        
        transactionTemplate.execute(status -> {
            try {
                permissionRepository.deleteRoleMenuByRoleId(roleId);
                permissionRepository.insertRoleMenu(roleId, menuIds);
                clearUserPermissionsCacheByRoleId(roleId);
                return null;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("分配角色权限失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "分配角色权限失败");
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(RoleAddOrUpdateRequest role) {
        if (permissionRepository.existsByCode(role.getCode())) {
            throw new BusinessException(ResponseCode.DUPLICATE_KEY.getCode(), "角色编码已存在");
        }

        Role roleEntity = Role.builder()
                .code(role.getCode())
                .name(role.getName())
                .remark(role.getRemark())
                .build();

        permissionRepository.saveRole(roleEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleAddOrUpdateRequest role) {
        Optional<Role> existingRoleOptional = permissionRepository.findRoleById(role.getId());
        if (!existingRoleOptional.isPresent()) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "角色不存在");
        }

        Role existingRole = existingRoleOptional.get();
        if (!existingRole.getCode().equals(role.getCode()) &&
                permissionRepository.existsByCode(role.getCode())) {
            throw new BusinessException(ResponseCode.DUPLICATE_KEY.getCode(), "角色编码已存在");
        }

        Role roleEntity = Role.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .remark(role.getRemark())
                .build();

        permissionRepository.saveRole(roleEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleByIds(List<Long> ids) {
        permissionRepository.deleteRoleByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(Menu menu) {
        if (menu.getType().equals(MenuTypeVO.CATALOG.getCode())) {
            menu.setComponent(MenuComponentVO.Layout.getName());
        }
        permissionRepository.saveMenu(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(Menu menu) {
        permissionRepository.saveMenu(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        permissionRepository.deleteMenu(id);
    }

    @Override
    public List<RouterVO> getCurrentUserMenu() {
        List<Menu> menus;
        try {
            if (StpUtil.hasRole("super_admin")) {
                menus = permissionRepository.findAllMenus();
            } else {
                List<Long> menuIds = permissionRepository.findMenuIdsByUserId(StpUtil.getLoginIdAsLong());
                menus = permissionRepository.findMenusByIds(menuIds);
            }
        } catch (Exception e) {
            log.error("获取当前用户菜单失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取当前用户菜单失败");
        }

        return buildRouterTree(menus);
    }

    /**
     * 构建菜单树形结构
     */
    public List<Menu> buildMenuTree(List<Menu> menuList) {
        Map<Long, Menu> menuMap = menuList.stream()
                .collect(Collectors.toMap(Menu::getId, menu -> menu));

        List<Menu> tree = new ArrayList<>();

        for (Menu menu : menuList) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                tree.add(menu);
            } else {
                Menu parent = menuMap.get(menu.getParentId());
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
     * 构建菜单选项树的子节点
     */
    private List<MenuOptionsVO> getOptionsChild(Long pid, List<Menu> menus) {
        if (menus == null) {
            return Collections.emptyList();
        }

        return menus.stream()
                .filter(menu -> menu.getParentId() != null && menu.getParentId().equals(pid))
                .map(menu -> MenuOptionsVO.builder()
                        .label(menu.getTitle())
                        .id(menu.getId())
                        .children(getOptionsChild(menu.getId(), menus))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 构建路由树（过滤BUTTON类型菜单）
     */
    private List<RouterVO> buildRouterTree(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        // 过滤掉BUTTON类型的菜单（按钮权限不需要生成路由）
        List<Menu> routeMenus = menus.stream()
                .filter(menu -> !"BUTTON".equals(menu.getType()))
                .collect(Collectors.toList());

        List<RouterVO> rootRouters = routeMenus.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .map(this::convertMenuToRouter)
                .sorted(Comparator.comparingInt(RouterVO::getSort))
                .collect(Collectors.toList());

        rootRouters.forEach(router -> router.setChildren(getRouterChild(router.getId(), routeMenus)));

        return rootRouters;
    }

    /**
     * 获取路由菜单树的子节点
     */
    private List<RouterVO> getRouterChild(Long parentId, List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        List<RouterVO> children = menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .map(this::convertMenuToRouter)
                .sorted(Comparator.comparingInt(RouterVO::getSort))
                .collect(Collectors.toList());

        children.forEach(child -> child.setChildren(getRouterChild(child.getId(), menus)));

        return children;
    }

    /**
     * 将菜单实体转换为路由实体
     */
    private RouterVO convertMenuToRouter(Menu menu) {
        RouterVO.MetaVO meta = new RouterVO.MetaVO(
                menu.getTitle(),
                menu.getIcon(),
                menu.getHidden()
        );

        return RouterVO.builder()
                .id(menu.getId())
                .path(menu.getPath())
                .redirect(menu.getRedirect())
                .name(menu.getName())
                .component(menu.getComponent())
                .meta(meta)
                .sort(menu.getSort())
                .build();
    }

    /**
     * 根据角色ID清除相关用户的权限缓存
     */
    private void clearUserPermissionsCacheByRoleId(Long roleId) {
        try {
            List<Long> userIds = permissionRepository.findUserIdsByRoleId(roleId);
            clearUserPermissionsCacheByUserIds(userIds);
            log.info("角色 [{}] 的菜单权限已变更，已清除 {} 个相关用户的权限缓存", roleId, userIds.size());
        } catch (Exception e) {
            log.error("清除角色相关用户权限缓存失败, roleId: {}", roleId, e);
        }
    }

    /**
     * 清除指定用户ID列表的权限缓存
     */
    private void clearUserPermissionsCacheByUserIds(List<Long> userIds) {
        if (userIds != null && !userIds.isEmpty()) {
            userIds.forEach(userPermission::clearUserPermissionCache);
        }
    }
}
