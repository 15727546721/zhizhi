package cn.xu.common.constants;

/**
 * 缓存相关常量
 * 
 * @author Kiro
 */
public class CacheConstants {

    private CacheConstants() {
        throw new IllegalStateException("常量类不允许实例化");
    }

    // ==================== 缓存过期时间（秒） ====================
    
    /**
     * 帖子浏览记录缓存时间 - 10分钟
     * <p>用于防止短时间内重复计数浏览量
     */
    public static final int POST_VIEW_CACHE_SECONDS = 600;
    
    /**
     * 验证码缓存时间 - 5分钟
     */
    public static final int VERIFICATION_CODE_CACHE_SECONDS = 300;
    
    /**
     * 密码重置令牌缓存时间 - 30分钟
     */
    public static final int PASSWORD_RESET_TOKEN_CACHE_SECONDS = 1800;
    
    /**
     * 用户会话缓存时间 - 24小时
     */
    public static final int USER_SESSION_CACHE_SECONDS = 86400;
    
    /**
     * 热门内容缓存时间 - 1小时
     */
    public static final int HOT_CONTENT_CACHE_SECONDS = 3600;
    
    /**
     * 统计数据缓存时间 - 5分钟
     */
    public static final int STATISTICS_CACHE_SECONDS = 300;

    // ==================== 缓存Key前缀 ====================
    
    /**
     * 帖子浏览记录Key前缀（IP）
     */
    public static final String POST_VIEW_IP_KEY_PREFIX = "post:view:ip:";
    
    /**
     * 帖子浏览记录Key前缀（用户）
     */
    public static final String POST_VIEW_USER_KEY_PREFIX = "post:view:user:";
    
    /**
     * 验证码Key前缀
     */
    public static final String VERIFICATION_CODE_KEY_PREFIX = "verification:code:";
    
    /**
     * 密码重置令牌Key前缀
     */
    public static final String PASSWORD_RESET_TOKEN_KEY_PREFIX = "password:reset:token:";
    
    /**
     * 评论热门分页缓存Key前缀
     */
    public static final String COMMENT_HOT_PAGE_KEY_PREFIX = "comment:hot:page:";

    // ==================== 缓存限制 ====================
    
    /**
     * 单次批量操作最大数量
     */
    public static final int MAX_BATCH_SIZE = 1000;
    
    /**
     * 缓存值最大长度（字节）
     */
    public static final int MAX_CACHE_VALUE_SIZE = 1024 * 1024; // 1MB
}
