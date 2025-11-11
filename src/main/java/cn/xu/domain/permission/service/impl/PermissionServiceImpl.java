package cn.xu.domain.permission.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.permission.RoleAddOrUpdateRequest;
import cn.xu.api.web.model.dto.permission.RoleMenuRequest;
import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.PageResponse;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.MenuOptionsEntity;
import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.model.entity.RouterEntity;
import cn.xu.domain.permission.model.vo.MenuComponentVO;
import cn.xu.domain.permission.model.vo.MenuTypeVO;
import cn.xu.domain.permission.repository.IPermissionRepository;
import cn.xu.domain.permission.service.IPermissionService;
import cn.xu.infrastructure.config.satoken.UserPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 * 实现权限相关的业务逻辑，遵循DDD领域驱动设计规范
 * 
 * @author Lily
 */
@Slf4j
@Service("permissionService")
public class PermissionServiceImpl implements IPermissionService {

    @Resource
    private IPermissionRepository permissionRepository;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private UserPermission userPermission;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MenuEntity> getMenuTreeList() {
        // 1查询所有菜单
        List<MenuEntity> menuEntityList = permissionRepository.findAllMenus();
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
        List<MenuEntity> menuEntityList = permissionRepository.findAllMenus();

        // 2组装成树形结构
        return menuEntityList.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .map(menu -> MenuOptionsEntity.builder()
                        .label(menu.getTitle())
                        .id(menu.getId())
                        .children(getOptionsChild(menu.getId(), menuEntityList))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MenuEntity> findMenuById(Long id) {
        return permissionRepository.findMenuById(id);
    }

    @Override
    public PageResponse<List<RoleEntity>> findRolePage(String name, int page, int size) {
        // 参数校验和默认值设置
        page = Math.max(1, page);  // 确保页码最小为1
        size = size <= 0 ? 10 : Math.min(size, 100);  // 确保每页数量在1-100之间

        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询数据
        return permissionRepository.findRolePage(name, offset, size);
    }

    @Override
    public List<Long> getCurrentUserRoleMenuIds() {
        long userId = StpUtil.getLoginIdAsLong();
        return permissionRepository.findMenuIdsByUserId(userId);
    }

    @Override
    public List<Long> findRoleMenuIdsById(Long roleId) {
        // 1. 检查角色是否存在
        Optional<RoleEntity> roleEntityOptional = permissionRepository.findRoleById(roleId);
        if (!roleEntityOptional.isPresent()) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "角色不存在");
        }

        RoleEntity roleEntity = roleEntityOptional.get();

        // 2. 如果是超级管理员角色，返回所有菜单ID
        if ("admin".equals(roleEntity.getCode())) {
            return permissionRepository.findAllMenuIds();
        }

        // 3. 获取角色的菜单权限
        List<Long> menuIds = permissionRepository.findMenuIdsByRoleId(roleId);
        if (menuIds == null) {
            menuIds = Collections.emptyList();
        }

        return menuIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenus(RoleMenuRequest roleMenuRequest) {
        Long roleId = roleMenuRequest.getRoleId();
        List<Long> menuIds = roleMenuRequest.getMenuIds();
        if (roleId != null && menuIds.isEmpty()) {
            // 将角色绑定的权限菜单清空
            permissionRepository.deleteRoleMenuByRoleId(roleId);
            // 清除与该角色关联的用户的权限缓存
            clearUserPermissionsCacheByRoleId(roleId);
            return;
        }
        if (roleId == null || menuIds.isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
        }
        
        Optional<RoleEntity> roleEntityOptional = permissionRepository.findRoleById(roleId);
        if (roleEntityOptional.isPresent() && roleEntityOptional.get().getCode().equals("admin")) {
            return;
        }
        
        transactionTemplate.execute(status -> {
            try {
                // 先删除原有菜单权限
                permissionRepository.deleteRoleMenuByRoleId(roleId);
                // 再插入新菜单权限
                permissionRepository.insertRoleMenu(roleId, menuIds);
                // 清除与该角色关联的用户的权限缓存
                clearUserPermissionsCacheByRoleId(roleId);
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
    @Transactional(rollbackFor = Exception.class)
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
        permissionRepository.saveRole(roleEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleAddOrUpdateRequest role) {
        // 1. 检查角色是否存在
        Optional<RoleEntity> existingRoleOptional = permissionRepository.findRoleById(role.getId());
        if (!existingRoleOptional.isPresent()) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "角色不存在");
        }

        RoleEntity existingRole = existingRoleOptional.get();

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

        permissionRepository.saveRole(roleEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleByIds(List<Long> ids) {
        permissionRepository.deleteRoleByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(MenuEntity menu) {
        if (menu.getType().equals(MenuTypeVO.CATALOG.getCode())) {
            menu.setComponent(MenuComponentVO.Layout.getName());
        }
        permissionRepository.saveMenu(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(MenuEntity menu) {
        permissionRepository.saveMenu(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        permissionRepository.deleteMenu(id);
    }

    @Override
    public List<RouterEntity> getCurrentUserMenu() {
        List<MenuEntity> menus;
        try {
            if (StpUtil.hasRole("admin")) {
                menus = permissionRepository.findAllMenus();
            } else {
                List<Long> menuIds = permissionRepository.findMenuIdsByUserId(StpUtil.getLoginIdAsLong());
                menus = permissionRepository.findMenusByIds(menuIds);
            }
        } catch (Exception e) {
            log.error("获取当前用户菜单失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取当前用户菜单失败");
        }

        List<RouterEntity> routerList = buildRouterTree(menus);
        return routerList;
    }

    /**
     * 构建菜单树形结构
     *
     * @param menuList 菜单列表
     * @return 树形结构的菜单列表
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
     * 构建菜单选项树的子节点
     *
     * @param pid 菜单父ID
     * @param menus 菜单列表
     * @return 子菜单选项列表
     */
    private List<MenuOptionsEntity> getOptionsChild(Long pid, List<MenuEntity> menus) {
        if (menus == null) {
            return Collections.emptyList();
        }

        return menus.stream()
                .filter(menu -> menu.getParentId() != null && menu.getParentId().equals(pid))
                .map(menu -> MenuOptionsEntity.builder()
                        .label(menu.getTitle())
                        .id(menu.getId())
                        .children(getOptionsChild(menu.getId(), menus))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 构建路由树
     *
     * @param menus 菜单列表
     * @return 路由树列表
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
     *
     * @param parentId 父节点ID
     * @param menus 菜单列表
     * @return 子节点路由列表
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
     *
     * @param menu 菜单实体
     * @return 路由实体
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

    /**
     * 根据角色ID清除相关用户的权限缓存
     * @param roleId 角色ID
     */
    private void clearUserPermissionsCacheByRoleId(Long roleId) {
        try {
            // 根据角色ID查询所有关联的用户ID
            List<Long> userIds = permissionRepository.findUserIdsByRoleId(roleId);
            // 清除这些用户的权限缓存
            clearUserPermissionsCacheByUserIds(userIds);
            log.info("角色 [{}] 的菜单权限已变更，已清除 {} 个相关用户的权限缓存", roleId, userIds.size());
        } catch (Exception e) {
            log.error("清除角色相关用户权限缓存失败, roleId: {}", roleId, e);
        }
    }
    
    /**
     * 清除指定用户ID列表的权限缓存
     * @param userIds 用户ID列表
     */
    private void clearUserPermissionsCacheByUserIds(List<Long> userIds) {
        if (userIds != null && !userIds.isEmpty()) {
            userIds.forEach(userPermission::clearUserPermissionCache);
        }
    }
}