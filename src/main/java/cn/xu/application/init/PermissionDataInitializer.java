package cn.xu.application.init;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.xu.api.system.model.dto.user.UserRequest;
import cn.xu.api.web.model.dto.permission.RoleAddOrUpdateRequest;
import cn.xu.api.web.model.dto.permission.RoleMenuRequest;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.model.vo.MenuTypeVO;
import cn.xu.domain.permission.repository.IRoleRepository;
import cn.xu.domain.permission.service.IPermissionService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserRoleService;
import cn.xu.domain.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 权限数据初始化器
 * 在应用启动时初始化默认的角色和菜单数据
 * 
 * @author Lily
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionDataInitializer implements CommandLineRunner {

    @Resource
    private IPermissionService permissionService;
    
    @Resource
    private IUserRoleService userRoleService;
    
    @Resource
    private IUserService userService;
    
    @Resource
    private IUserRepository userRepository;
    
    @Resource
    private IRoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化权限数据...");

        try {
            // 检查是否已经存在角色数据
            long roleCount = permissionService.countRole("");
            if (roleCount > 0) {
                log.info("权限数据已存在，跳过初始化");
                return;
            }
            
            // 初始化默认角色
            initDefaultRoles();
            
            // 初始化默认菜单
            initDefaultMenus();
            
            // 初始化默认用户并关联角色
            initDefaultUser();
            
            log.info("权限数据初始化完成");
        } catch (Exception e) {
            log.error("权限数据初始化失败", e);
        }
    }
    
    /**
     * 初始化默认角色
     */
    private void initDefaultRoles() {
        log.info("初始化默认角色...");
        
        try {
            // 创建超级管理员角色
            RoleAddOrUpdateRequest adminRole = new RoleAddOrUpdateRequest();
            adminRole.setCode("admin");
            adminRole.setName("超级管理员");
            adminRole.setRemark("系统超级管理员，拥有所有权限");
            permissionService.addRole(adminRole);
            
            // 创建普通用户角色
            RoleAddOrUpdateRequest userRole = new RoleAddOrUpdateRequest();
            userRole.setCode("user");
            userRole.setName("普通用户");
            userRole.setRemark("系统普通用户");
            permissionService.addRole(userRole);
            
            log.info("默认角色初始化完成");
        } catch (Exception e) {
            log.error("初始化默认角色失败", e);
            throw e;
        }
    }
    
    /**
     * 初始化默认菜单
     */
    private void initDefaultMenus() {
        log.info("初始化默认菜单...");
        
        try {
            // 创建系统管理目录
            MenuEntity systemMenu = MenuEntity.builder()
                    .parentId(0L)
                    .path("/system")
                    .component("Layout")
                    .title("系统管理")
                    .sort(1)
                    .icon("system")
                    .type(MenuTypeVO.CATALOG.getCode())
                    .createTime(new java.util.Date())
                    .updateTime(new java.util.Date())
                    .redirect("/system/user")
                    .name("System")
                    .hidden(0)
                    .perm(null)
                    .build();
            permissionService.addMenu(systemMenu);
            
            // 重新查询系统菜单以获取实际ID
            List<MenuEntity> allMenus = permissionService.selectMenuTreeList();
            MenuEntity savedSystemMenu = findMenuByTitle(allMenus, "系统管理");
            
            if (savedSystemMenu == null) {
                log.error("无法找到已保存的系统管理菜单");
                return;
            }
            
            // 创建用户管理菜单
            MenuEntity userMenu = MenuEntity.builder()
                    .parentId(savedSystemMenu.getId())
                    .path("user")
                    .component("system/user/index")
                    .title("用户管理")
                    .sort(1)
                    .icon("user")
                    .type(MenuTypeVO.MENU.getCode())
                    .createTime(new java.util.Date())
                    .updateTime(new java.util.Date())
                    .name("User")
                    .hidden(0)
                    .perm("system:user:list")
                    .build();
            permissionService.addMenu(userMenu);
            
            // 创建角色管理菜单
            MenuEntity roleMenu = MenuEntity.builder()
                    .parentId(savedSystemMenu.getId())
                    .path("role")
                    .component("system/role/index")
                    .title("角色管理")
                    .sort(2)
                    .icon("peoples")
                    .type(MenuTypeVO.MENU.getCode())
                    .createTime(new java.util.Date())
                    .updateTime(new java.util.Date())
                    .name("Role")
                    .hidden(0)
                    .perm("system:role:list")
                    .build();
            permissionService.addMenu(roleMenu);
            
            // 创建菜单管理菜单
            MenuEntity menuMenu = MenuEntity.builder()
                    .parentId(savedSystemMenu.getId())
                    .path("menu")
                    .component("system/menu/index")
                    .title("菜单管理")
                    .sort(3)
                    .icon("tree-table")
                    .type(MenuTypeVO.MENU.getCode())
                    .createTime(new java.util.Date())
                    .updateTime(new java.util.Date())
                    .name("Menu")
                    .hidden(0)
                    .perm("system:menu:list")
                    .build();
            permissionService.addMenu(menuMenu);
            
            log.info("默认菜单初始化完成");
        } catch (Exception e) {
            log.error("初始化默认菜单失败", e);
            throw e;
        }
    }
    
    /**
     * 初始化默认用户并关联角色
     */
    private void initDefaultUser() {
        log.info("初始化默认用户...");
        
        try {
            // 创建默认超级管理员用户
            UserRequest adminUserRequest = new UserRequest();
            adminUserRequest.setUsername("admin");
            adminUserRequest.setPassword("admin123"); // 不再在这里加密，让UserEntity自动处理
            adminUserRequest.setNickname("超级管理员");
            adminUserRequest.setEmail("admin@example.com");
            adminUserRequest.setStatus(0); // 0表示正常状态
            
            // 保存用户
            userService.addUser(adminUserRequest);
            log.info("默认超级管理员用户创建成功");
            
            // 查询刚创建的用户和角色
            UserEntity userEntity = getUserEntityByUsername("admin");
            RoleEntity roleEntity = roleRepository.findByCode("admin");
            
            if (userEntity == null || roleEntity == null) {
                log.error("无法获取用户或角色，用户: {}, 角色: {}", userEntity, roleEntity);
                return;
            }
            
            Long userId = userEntity.getId();
            Long roleId = roleEntity.getId();
            
            // 重新查询菜单列表以获取所有菜单ID
            List<MenuEntity> allMenus = permissionService.selectMenuTreeList();
            List<Long> allMenuIds = getAllMenuIds(allMenus);
            
            // 为超级管理员角色分配所有菜单权限
            RoleMenuRequest roleMenuRequest = new RoleMenuRequest();
            roleMenuRequest.setRoleId(roleId);
            roleMenuRequest.setMenuIds(allMenuIds);
            permissionService.assignRoleMenus(roleMenuRequest);
            log.info("为超级管理员角色分配菜单权限成功");
            
            // 为用户分配角色
            userRoleService.assignRolesToUser(userId, Arrays.asList(roleId));
            log.info("为默认用户分配角色成功");
            
        } catch (Exception e) {
            log.error("创建默认用户失败", e);
            throw e;
        }
        
        log.info("默认用户初始化完成");
    }
    
    /**
     * 根据用户名获取用户实体
     */
    private UserEntity getUserEntityByUsername(String username) {
        // 重试机制，因为数据库操作可能有延迟
        for (int i = 0; i < 3; i++) {
            UserEntity userEntity = userRepository.findUserEntityByUsername(username);
            if (userEntity != null) {
                return userEntity;
            }
            try {
                Thread.sleep(100); // 等待100毫秒后重试
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }
    
    /**
     * 根据菜单标题查找菜单
     */
    private MenuEntity findMenuByTitle(List<MenuEntity> menus, String title) {
        if (menus == null || menus.isEmpty()) {
            return null;
        }
        
        for (MenuEntity menu : menus) {
            if (title.equals(menu.getTitle())) {
                return menu;
            }
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                MenuEntity found = findMenuByTitle(menu.getChildren(), title);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    /**
     * 获取所有菜单ID
     */
    private List<Long> getAllMenuIds(List<MenuEntity> menus) {
        List<Long> ids = new ArrayList<>();
        if (menus == null || menus.isEmpty()) {
            return ids;
        }
        
        for (MenuEntity menu : menus) {
            ids.add(menu.getId());
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                ids.addAll(getAllMenuIds(menu.getChildren()));
            }
        }
        return ids;
    }
}