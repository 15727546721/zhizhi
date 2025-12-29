package cn.xu.common.constants;

/**
 * 权限常量
 * 
 * <p>定义系统中的权限字符串</p>
 */
public final class PermissionConstants {
    
    private PermissionConstants() {
        // 防止实例化
    }
    
    // ==================== 超级管理员权限 ====================
    
    /** 所有权限 */
    public static final String ALL = "*:*:*";
    
    // ==================== 用户管理权限 ====================
    
    /** 用户列表 */
    public static final String USER_LIST = "system:user:list";
    
    /** 用户查询 */
    public static final String USER_QUERY = "system:user:query";
    
    /** 用户新增 */
    public static final String USER_ADD = "system:user:add";
    
    /** 用户编辑 */
    public static final String USER_EDIT = "system:user:edit";
    
    /** 用户删除 */
    public static final String USER_DELETE = "system:user:delete";
    
    /** 踢出用户 */
    public static final String USER_KICK = "system:user:kick";
    
    // ==================== 帖子管理权限 ====================
    
    /** 帖子所有权限 */
    public static final String POST_ALL = "system:post:*";
    
    /** 帖子列表 */
    public static final String POST_LIST = "system:post:list";
    
    /** 帖子新增 */
    public static final String POST_ADD = "system:post:add";
    
    /** 帖子更新 */
    public static final String POST_UPDATE = "system:post:update";
    
    /** 帖子删除 */
    public static final String POST_DELETE = "system:post:delete";
    
    /** 帖子发布 */
    public static final String POST_PUBLISH = "system:post:publish";
    
    /** 帖子置顶 */
    public static final String POST_TOP = "system:post:top";
    
    // ==================== 评论管理权限 ====================
    
    /** 评论所有权限 */
    public static final String COMMENT_ALL = "system:comment:*";
    
    /** 评论列表 */
    public static final String COMMENT_LIST = "system:comment:list";
    
    /** 评论删除 */
    public static final String COMMENT_DELETE = "system:comment:delete";
    
    // ==================== 标签管理权限 ====================
    
    /** 标签所有权限 */
    public static final String TAG_ALL = "system:tag:*";
    
    /** 标签列表 */
    public static final String TAG_LIST = "system:tag:list";
    
    /** 标签新增 */
    public static final String TAG_ADD = "system:tag:add";
    
    /** 标签更新 */
    public static final String TAG_UPDATE = "system:tag:update";
    
    /** 标签删除 */
    public static final String TAG_DELETE = "system:tag:delete";
    
    // ==================== 统计权限 ====================
    
    /** 统计查看 */
    public static final String STATISTICS_VIEW = "system:statistics:view";
    
    // ==================== 角色管理权限 ====================
    
    /** 角色列表 */
    public static final String ROLE_LIST = "system:role:list";
    
    /** 角色新增 */
    public static final String ROLE_ADD = "system:role:add";
    
    /** 角色更新 */
    public static final String ROLE_UPDATE = "system:role:update";
    
    /** 角色删除 */
    public static final String ROLE_DELETE = "system:role:delete";
    
    // ==================== 菜单管理权限 ====================
    
    /** 菜单列表 */
    public static final String MENU_LIST = "system:menu:list";
    
    /** 菜单新增 */
    public static final String MENU_ADD = "system:menu:add";
    
    /** 菜单更新 */
    public static final String MENU_UPDATE = "system:menu:update";
    
    /** 菜单删除 */
    public static final String MENU_DELETE = "system:menu:delete";
    
    // ==================== 安全管理权限 ====================
    
    /** IP管理 */
    public static final String SECURITY_IP = "system:security:ip";
}
