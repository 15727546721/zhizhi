package cn.xu.service.permission;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.constants.RoleConstants;
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
import cn.xu.repository.PermissionRepository;
import cn.xu.support.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 * <p>负责角色、菜单、权限的管理</p>
 
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private PermissionRepository permissionRepository;
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
        Role roleEntity = permissionRepository.findRoleById(roleId)
                .orElseThrow(() -> new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "角色不存在"));

        if (RoleConstants.CODE_ADMIN.equals(roleEntity.getCode())) {
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
        if (roleEntityOptional.isPresent() && RoleConstants.CODE_ADMIN.equals(roleEntityOptional.get().getCode())) {
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
                log.error("处理角色菜单关联时发生错误", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "处理角色菜单关联时发生错误");
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
        Role existingRole = permissionRepository.findRoleById(role.getId())
                .orElseThrow(() -> new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "角色不存在"));

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
            if (StpUtil.hasRole(RoleConstants.CODE_SUPER_ADMIN)) {
                menus = permissionRepository.findAllMenus();
            } else {
                List<Long> menuIds = permissionRepository.findMenuIdsByUserId(StpUtil.getLoginIdAsLong());
                menus = permissionRepository.findMenusByIds(menuIds);
            }
        } catch (Exception e) {
            log.error("获取当前用户菜单时发生错误", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取当前用户菜单时发生错误");
        }

        return buildRouterTree(menus);
    }
    
    /**
     * 构建菜单树
     */
    private List<Menu> buildMenuTree(List<Menu> menuList) {
        if (menuList == null || menuList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 找出所有根节点
        List<Menu> rootMenus = menuList.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .collect(Collectors.toList());
        
        // 为每个根节点设置子节点
        rootMenus.forEach(root -> setChildren(root, menuList));
        
        return rootMenus;
    }
    
    /**
     * 递归设置子菜单
     */
    private void setChildren(Menu parent, List<Menu> allMenus) {
        List<Menu> children = allMenus.stream()
                .filter(menu -> parent.getId().equals(menu.getParentId()))
                .collect(Collectors.toList());
        
        if (!children.isEmpty()) {
            children.forEach(child -> setChildren(child, allMenus));
            parent.setChildren(children);
        }
    }
    
    /**
     * 获取菜单选项的子节点
     */
    private List<MenuOptionsVO> getOptionsChild(Long parentId, List<Menu> allMenus) {
        return allMenus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .map(menu -> MenuOptionsVO.builder()
                        .label(menu.getTitle())
                        .id(menu.getId())
                        .children(getOptionsChild(menu.getId(), allMenus))
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * 清除角色相关用户的权限缓存
     */
    private void clearUserPermissionsCacheByRoleId(Long roleId) {
        // 简化实现：暂不做缓存清理
        log.debug("清除角色 {} 相关用户的权限缓存", roleId);
    }
    
    /**
     * 构建路由树
     */
    private List<RouterVO> buildRouterTree(List<Menu> menuList) {
        if (menuList == null || menuList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 找出所有根节点
        List<Menu> rootMenus = menuList.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .sorted(Comparator.comparingInt(m -> m.getSort() != null ? m.getSort() : 0))
                .collect(Collectors.toList());
        
        // 递归构建路由树
        return rootMenus.stream()
                .map(menu -> buildRouterVO(menu, menuList))
                .collect(Collectors.toList());
    }
    
    /**
     * 构建单个路由VO
     */
    private RouterVO buildRouterVO(Menu menu, List<Menu> allMenus) {
        RouterVO router = new RouterVO();
        router.setPath(menu.getPath());
        router.setName(menu.getName());
        router.setComponent(menu.getComponent());
        router.setRedirect(menu.getRedirect());
        
        // 设置 meta 信息
        RouterVO.MetaVO meta = new RouterVO.MetaVO();
        meta.setTitle(menu.getTitle());
        meta.setIcon(menu.getIcon());
        meta.setHidden(menu.getHidden());
        router.setMeta(meta);
        
        // 递归设置子路由
        List<Menu> children = allMenus.stream()
                .filter(m -> menu.getId().equals(m.getParentId()))
                .sorted(Comparator.comparingInt(m -> m.getSort() != null ? m.getSort() : 0))
                .collect(Collectors.toList());
        
        if (!children.isEmpty()) {
            router.setChildren(children.stream()
                    .map(child -> buildRouterVO(child, allMenus))
                    .collect(Collectors.toList()));
        }
        
        return router;
    }
}
