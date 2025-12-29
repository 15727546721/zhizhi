package cn.xu.common.constants;

/**
 * 角色常量
 * 
 * <p>定义系统中的角色编码和角色名称</p>
 */
public final class RoleConstants {
    
    private RoleConstants() {
        // 防止实例化
    }
    
    // ==================== 角色编码 ====================
    
    /** 超级管理员角色编码 */
    public static final String CODE_ADMIN = "admin";
    
    /** 超级管理员角色编码（Sa-Token角色标识） */
    public static final String CODE_SUPER_ADMIN = "super_admin";
    
    /** 普通用户角色编码 */
    public static final String CODE_USER = "user";
    
    /** 官方账号角色编码 */
    public static final String CODE_OFFICIAL = "official";
    
    // ==================== 角色名称 ====================
    
    /** 超级管理员角色名称 */
    public static final String NAME_ROOT = "ROOT";
    
    /** 管理员角色名称 */
    public static final String NAME_ADMIN = "ADMIN";
    
    /** 普通用户角色名称 */
    public static final String NAME_USER = "USER";
    
    // ==================== 默认账号 ====================
    
    /** 默认管理员用户名 */
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    
    /** 默认管理员密码 */
    public static final String DEFAULT_ADMIN_PASSWORD = "AdminPassword123!";
    
    /** 默认管理员邮箱 */
    public static final String DEFAULT_ADMIN_EMAIL = "admin@zhizhi.com";
    
    /** 默认管理员昵称 */
    public static final String DEFAULT_ADMIN_NICKNAME = "系统管理员";
}
