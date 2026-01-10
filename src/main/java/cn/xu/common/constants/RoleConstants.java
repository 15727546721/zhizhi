package cn.xu.common.constants;

/**
 * 角色常量
 * 
 * <p>定义系统中的角色编码和角色名称</p>
 * <p>注意：默认管理员凭证已移至配置文件 application.yml 中的 app.admin 配置项</p>
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
    
    // ==================== 默认账号配置键 ====================
    // 注意：实际值应从配置文件读取，这里只保留配置键名
    
    /** 默认管理员用户名配置键 */
    public static final String CONFIG_KEY_ADMIN_USERNAME = "app.admin.username";
    
    /** 默认管理员密码配置键 */
    public static final String CONFIG_KEY_ADMIN_PASSWORD = "app.admin.password";
    
    /** 默认管理员邮箱配置键 */
    public static final String CONFIG_KEY_ADMIN_EMAIL = "app.admin.email";
    
    /** 默认管理员昵称 */
    public static final String DEFAULT_ADMIN_NICKNAME = "系统管理员";
}
