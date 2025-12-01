package cn.xu.config;

import cn.xu.controller.admin.model.dto.user.SysUserRequest;
import cn.xu.model.dto.permission.RoleAddOrUpdateRequest;
import cn.xu.model.dto.permission.RoleMenuRequest;
import cn.xu.model.entity.Menu;
import cn.xu.model.entity.Role;
import cn.xu.model.entity.User;
import cn.xu.model.vo.permission.MenuTypeVO;
import cn.xu.repository.IRoleRepository;
import cn.xu.repository.IUserRepository;
import cn.xu.service.permission.PermissionService;
import cn.xu.service.user.IUserService;
import cn.xu.service.user.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 权限数据初始化
 * 用于在系统启动时初始化默认的角色、菜单和用户数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionDataInitializer implements CommandLineRunner {

    @Resource
    private PermissionService permissionService;
    
    @Resource
    private UserRoleService userRoleService;
    
    @Resource
    private IUserService userService;
    
    @Resource
    private IUserRepository userRepository;
    
    @Resource
    private IRoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("权限数据初始化...");

        try {
            // 检查是否已经存在角色数据
            long roleCount = permissionService.findRolePage("", 1, 10).getTotal();
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
            Menu systemMenu = Menu.builder()
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
            List<Menu> allMenus = permissionService.getMenuTreeList();
            Menu savedSystemMenu = findMenuByTitle(allMenus, "系统管理");
            
            if (savedSystemMenu == null) {
                log.error("无法找到已保存的系统管理菜单");
                return;
            }
            
            // 创建用户管理菜单
            Menu userMenu = Menu.builder()
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
            Menu roleMenu = Menu.builder()
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
            Menu menuMenu = Menu.builder()
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
            SysUserRequest adminUserRequest = new SysUserRequest();
            adminUserRequest.setUsername("admin");
            adminUserRequest.setPassword("admin123");
            adminUserRequest.setNickname("超级管理员");
            adminUserRequest.setEmail("admin@example.com");
            adminUserRequest.setStatus(0);
            
            // 保存用户
            userService.addUser(adminUserRequest);
            log.info("默认超级管理员用户创建成功");
            
            // 查询超级管理员用户和角色
            User userEntity = getUserEntityByUsername("admin");
            Role roleEntity = roleRepository.findByCode("admin");
            
            if (userEntity == null || roleEntity == null) {
                log.error("无法找到用户或角色实体");
                return;
            }
            
            Long userId = userEntity.getId();
            Long roleId = roleEntity.getId();
            
            // 获取所有菜单的ID
            List<Menu> allMenus = permissionService.getMenuTreeList();
            List<Long> allMenuIds = getAllMenuIds(allMenus);
            
            // 为超级管理员角色分配菜单权限
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
     * 根据用户名查询用户实体
     */
    private User getUserEntityByUsername(String username) {
        // 重试3次，如果找不到用户，则返回null
        for (int i = 0; i < 3; i++) {
            User userEntity = userRepository.findUserEntityByUsername(username);
            if (userEntity != null) {
                return userEntity;
            }
            try {
                Thread.sleep(100); // 等待100毫秒再试
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
    private Menu findMenuByTitle(List<Menu> menus, String title) {
        if (menus == null || menus.isEmpty()) {
            return null;
        }
        
        for (Menu menu : menus) {
            if (title.equals(menu.getTitle())) {
                return menu;
            }
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                Menu found = findMenuByTitle(menu.getChildren(), title);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    /**
     * 获取所有菜单的ID
     */
    private List<Long> getAllMenuIds(List<Menu> menus) {
        List<Long> ids = new ArrayList<>();
        if (menus == null || menus.isEmpty()) {
            return ids;
        }
        
        for (Menu menu : menus) {
            ids.add(menu.getId());
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                ids.addAll(getAllMenuIds(menu.getChildren()));
            }
        }
        return ids;
    }
}
