package cn.xu.common.constants;

/**
 * Redis Key 常量
 * 
 * <p>统一管理系统中所有的Redis Key前缀，避免分散定义导致的维护困难</p>
 * 
 * <p>命名规范：业务模块:功能:具体标识</p>
 */
public final class RedisKeyConstants {
    
    private RedisKeyConstants() {
        // 防止实例化
    }
    
    // ==================== 验证码相关 ====================
    
    /** 验证码 */
    public static final String VERIFY_CODE = "verify:code:";
    
    /** 验证码发送间隔 */
    public static final String VERIFY_INTERVAL = "verify:interval:";
    
    /** 每日验证码发送次数 */
    public static final String VERIFY_DAILY_COUNT = "verify:daily:";
    
    // ==================== 密码重置相关 ====================
    
    /** 密码重置令牌 */
    public static final String PASSWORD_RESET_TOKEN = "password:reset:token:";
    
    /** 密码重置邮箱 */
    public static final String PASSWORD_RESET_EMAIL = "password:reset:email:";
    
    /** 每日密码重置次数 */
    public static final String PASSWORD_RESET_DAILY = "password:reset:daily:";
    
    // ==================== 登录安全相关 ====================
    
    /** 登录失败次数 */
    public static final String LOGIN_FAIL_COUNT = "login:fail:";
    
    /** 登录锁定 */
    public static final String LOGIN_LOCK = "login:lock:";
    
    // ==================== IP封禁相关 ====================
    
    /** IP封禁 */
    public static final String IP_BLOCKED = "ip:blocked:";
    
    /** 可疑IP */
    public static final String IP_SUSPICIOUS = "ip:suspicious:";
    
    /** IP白名单 */
    public static final String IP_WHITELIST = "ip:whitelist";
    
    /** 永久封禁IP */
    public static final String IP_PERMANENT_BLOCKED = "ip:permanent:blocked";
    
    // ==================== 搜索相关 ====================
    
    /** 用户搜索历史 */
    public static final String SEARCH_HISTORY = "search:history:";
    
    /** 热门搜索词 */
    public static final String SEARCH_HOT = "search:hot";
    
    /** 每日热门搜索词 */
    public static final String SEARCH_HOT_DAILY = "search:hot:daily";
    
    /** 搜索防刷 */
    public static final String SEARCH_ANTISPAM = "post:search:antispam:";
    
    /** 搜索统计 */
    public static final String SEARCH_STATS = "post:search:stats:";
    
    // ==================== 帖子相关 ====================
    
    /** 帖子热度分数 */
    public static final String POST_HOT_SCORE = "post:hot:score:";
    
    /** 帖子浏览记录（IP） */
    public static final String POST_VIEW_IP = "post:view:ip:";
    
    /** 帖子浏览记录（用户） */
    public static final String POST_VIEW_USER = "post:view:user:";
    
    // ==================== 评论相关 ====================
    
    /** 热门评论分页缓存 */
    public static final String COMMENT_HOT_PAGE = "comment:hot:page:";
    
    // ==================== 私信限流相关 ====================
    
    /** 私信用户限流 */
    public static final String PM_RATE_USER = "pm:rate:user:";
    
    // ==================== ES索引相关 ====================
    
    /** ES索引失败任务 */
    public static final String ES_INDEX_FAILED_TASKS = "es:index:failed:tasks";
    
    // ==================== 缓存相关 ====================
    
    /** 缓存清理锁 */
    public static final String CACHE_CLEAR_LOCK = "cache:clear:lock:";
    
    /** 健康检查 */
    public static final String HEALTH_CHECK = "health_check";
    
    // ==================== 权限相关 ====================
    
    /** 角色列表缓存 */
    public static final String ROLE_LIST = "ROLE_LIST";
    
    /** 权限列表缓存 */
    public static final String PERMISSION_LIST = "PERMISSION_LIST";
    
    // ==================== UV/PV 统计相关 ====================
    
    /** 每日UV统计（HyperLogLog） */
    public static final String STATS_UV_DAILY = "stats:uv:daily:";
    
    /** 每日PV统计 */
    public static final String STATS_PV_DAILY = "stats:pv:daily:";
    
    /** 总UV统计（HyperLogLog） */
    public static final String STATS_UV_TOTAL = "stats:uv:total";
    
    /** 总PV统计 */
    public static final String STATS_PV_TOTAL = "stats:pv:total";
    
    // ==================== 工具方法 ====================
    
    /**
     * 构建带参数的Key
     * @param prefix Key前缀
     * @param params 参数
     * @return 完整的Key
     */
    public static String buildKey(String prefix, Object... params) {
        if (params == null || params.length == 0) {
            return prefix;
        }
        StringBuilder sb = new StringBuilder(prefix);
        for (Object param : params) {
            sb.append(param).append(":");
        }
        // 移除最后一个冒号
        return sb.substring(0, sb.length() - 1);
    }
}
