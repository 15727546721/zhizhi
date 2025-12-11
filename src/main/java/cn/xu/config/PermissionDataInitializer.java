package cn.xu.config;

import cn.xu.model.entity.Menu;
import cn.xu.model.entity.Role;
import cn.xu.model.entity.User;
import cn.xu.repository.IRoleRepository;
import cn.xu.repository.IUserRepository;
import cn.xu.service.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 权限数据初始化检查器
 *
 * <p>职责：检查权限数据完整性，确保系统正常运行
 * <p>主初始化：由SQL脚本（02_data.sql）完成
 * <p>本组件：作为守护者，检查并报告数据问题
 *
 * <h3>初始化策略</h3>
 * <pre>
 * ├─────────────────────────────────────────────────────────────┤
 * │ 首次部署  执行 02_data.sql（完整初始化）              │
 * │ 日常启动  本组件检查数据完整性                        │
 * │ 数据缺失  打印警告，提示执行SQL脚本                     │
 * ├─────────────────────────────────────────────────────────────┤
 * </pre>
 *
 *
 */
@Slf4j
@Component
@Order(100) // 较低优先级，在其他初始化完成后执行
@RequiredArgsConstructor
public class PermissionDataInitializer implements CommandLineRunner {

    @Resource
    private PermissionService permissionService;

    @Resource
    private IUserRepository userRepository;

    @Resource
    private IRoleRepository roleRepository;

    /**
     * 是否启用数据完整性检查
     */
    @Value("${app.permission.check-on-startup:true}")
    private boolean checkOnStartup;

    /**
     * 是否在数据缺失时打印详细警告
     */
    @Value("${app.permission.verbose-warning:true}")
    private boolean verboseWarning;

    // ==================== 预期的初始化数据 ===================

    /** 预期的角色编码列表*/
    private static final String[] EXPECTED_ROLES = {
            "super_admin", "content_admin", "user_admin", "viewer"
    };

    /** 预期的管理员用户名*/
    private static final String ADMIN_USERNAME = "admin";

    /** 预期的最少菜单数 */
    private static final int MIN_MENU_COUNT = 20;

    @Override
    public void run(String... args) {
        if (!checkOnStartup) {
            log.debug("权限数据检查已禁用");
            return;
        }

        log.info("========== 权限数据完整性检查 ==========");

        try {
            PermissionCheckResult result = checkPermissionData();

            if (result.isComplete()) {
                log.info("权限数据完整性检查通过");
                log.info("   - 角色: {}", result.roleCount);
                log.info("   - 菜单: {}", result.menuCount);
                log.info("   - 管理员: {}", result.hasAdmin ? "存在" : "不存在");
            } else {
                printDataMissingWarning(result);
            }
        } catch (Exception e) {
            log.error("权限数据检查异常", e);
        }

        log.info("=====================================");
    }

    /**
     * 检查权限数据完整性
     */
    private PermissionCheckResult checkPermissionData() {
        PermissionCheckResult result = new PermissionCheckResult();

        // 1. 检查角色
        result.roleCount = countRoles();
        result.missingRoles = findMissingRoles();

        // 2. 检查菜单
        result.menuCount = countMenus();

        // 3. 检查管理员用户
        result.hasAdmin = checkAdminUser();

        // 4. 检查管理员角色关联
        result.adminHasRole = checkAdminHasRole();

        return result;
    }

    /**
     * 统计角色数量
     */
    private int countRoles() {
        try {
            Long total = permissionService.findRolePage("", 1, 100).getTotal();
            return total != null ? total.intValue() : 0;
        } catch (Exception e) {
            log.warn("统计角色数量失败", e);
            return 0;
        }
    }

    /**
     * 查找缺失的角色
     */
    private String findMissingRoles() {
        StringBuilder missing = new StringBuilder();
        for (String roleCode : EXPECTED_ROLES) {
            Role role = roleRepository.findByCode(roleCode);
            if (role == null) {
                if (missing.length() > 0) {
                    missing.append(", ");
                }
                missing.append(roleCode);
            }
        }
        return missing.toString();
    }

    /**
     * 统计菜单数量
     */
    private int countMenus() {
        try {
            List<Menu> menus = permissionService.getMenuTreeList();
            return countMenusRecursive(menus);
        } catch (Exception e) {
            log.warn("统计菜单数量失败", e);
            return 0;
        }
    }

    private int countMenusRecursive(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return 0;
        }
        int count = menus.size();
        for (Menu menu : menus) {
            if (menu.getChildren() != null) {
                count += countMenusRecursive(menu.getChildren());
            }
        }
        return count;
    }

    /**
     * 检查管理员用户是否存在
     */
    private boolean checkAdminUser() {
        try {
            User admin = userRepository.findByUsername(ADMIN_USERNAME).orElse(null);
            return admin != null;
        } catch (Exception e) {
            log.warn("检查管理员用户失败", e);
            return false;
        }
    }

    /**
     * 检查管理员是否关联了角色
     */
    private boolean checkAdminHasRole() {
        try {
            User admin = userRepository.findByUsername(ADMIN_USERNAME).orElse(null);
            if (admin == null) {
                return false;
            }
            List<String> roles = userRepository.findRolesByUserId(admin.getId());
            return roles != null && !roles.isEmpty();
        } catch (Exception e) {
            log.warn("检查管理员角色关联失败", e);
            return false;
        }
    }

    /**
     * 打印数据缺失警告
     */
    private void printDataMissingWarning(PermissionCheckResult result) {
        log.warn("⚠ 权限数据不完整！");

        if (!verboseWarning) {
            log.warn("请执行SQL脚本初始化： resources/sql/02_data.sql");
            return;
        }

        StringBuilder warning = new StringBuilder();
        warning.append("\n");
        warning.append("┌─────────────────────────────────────────────────────────────┐\n");
        warning.append("⚠ 权限数据不完整，请执行初始化脚本\n");
        warning.append("└─────────────────────────────────────────────────────────────┘\n");

        // 检查结果
        warning.append("检查结果：\n");
        warning.append(String.format("  - 角色数量: %-3d (预期: %-3d) %s\n",
                result.roleCount, EXPECTED_ROLES.length,
                result.roleCount >= EXPECTED_ROLES.length ? "OK" : "缺失"));
        warning.append(String.format("  - 菜单数量: %-3d (预期: %-3d) %s\n",
                result.menuCount, MIN_MENU_COUNT,
                result.menuCount >= MIN_MENU_COUNT ? "OK" : "缺失"));
        warning.append(String.format("  - 管理员用户: %s\n",
                result.hasAdmin ? "存在" : "不存在"));
        warning.append(String.format("  - 管理员角色: %s\n",
                result.adminHasRole ? "已关联" : "未关联"));

        if (!result.missingRoles.isEmpty()) {
            warning.append(String.format("  - 缺失角色: %s\n", result.missingRoles));
        }

        warning.append("└─────────────────────────────────────────────────────────────┘\n");
        warning.append("解决方案：\n");
        warning.append("  1. 执行表结构脚本：resources/sql/01_schema.sql\n");
        warning.append("  2. 执行数据脚本：resources/sql/02_data.sql\n");

        log.warn(warning.toString());
    }

    /**
     * 权限检查结果
     */
    private static class PermissionCheckResult {
        int roleCount = 0;
        int menuCount = 0;
        boolean hasAdmin = false;
        boolean adminHasRole = false;
        String missingRoles = "";

        boolean isComplete() {
            return roleCount >= EXPECTED_ROLES.length
                    && menuCount >= MIN_MENU_COUNT
                    && hasAdmin
                    && adminHasRole
                    && missingRoles.isEmpty();
        }
    }
}