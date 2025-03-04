package cn.xu.infrastructure.cache;

/**
 * Redis Key常量管理类
 * 命名规范：业务:类型:操作:ID
 * 例如：article:like:count:1
 */
public class RedisKeyConstants {
    
    // 过期时间常量（秒）
    public static final long EXPIRE_TIME_1_MIN = 60;
    public static final long EXPIRE_TIME_5_MIN = 300;
    public static final long EXPIRE_TIME_10_MIN = 600;
    public static final long EXPIRE_TIME_30_MIN = 1800;
    public static final long EXPIRE_TIME_1_HOUR = 3600;
    public static final long EXPIRE_TIME_1_DAY = 86400;
    public static final long EXPIRE_TIME_1_WEEK = 604800;
    public static final long EXPIRE_TIME_1_MONTH = 2592000;
    
    // 文章相关
    public static final String ARTICLE_BASE = "article:";
    public static final String ARTICLE_DETAIL = ARTICLE_BASE + "detail:";     // Hash，文章详情缓存
    public static final String ARTICLE_VIEW_COUNT = ARTICLE_BASE + "view:count:";   // String，文章浏览量
    public static final String ARTICLE_LIKE_COUNT = ARTICLE_BASE + "like:count:";   // String，文章点赞数
    public static final String ARTICLE_COLLECT_COUNT = ARTICLE_BASE + "collect:count:"; // String，文章收藏数
    public static final String ARTICLE_COMMENT_COUNT = ARTICLE_BASE + "comment:count:"; // String，文章评论数
    public static final String ARTICLE_HOT_SCORE = ARTICLE_BASE + "hot:score";   // ZSet，文章热度分数
    public static final String ARTICLE_VIEW_IP = ARTICLE_BASE + "view:ip:";      // HyperLogLog，文章访问IP去重
    
    // 用户相关
    public static final String USER_BASE = "user:";
    public static final String USER_INFO = USER_BASE + "info:";        // Hash，用户信息缓存
    public static final String USER_FOLLOWING = USER_BASE + "following:";  // Set，用户关注列表
    public static final String USER_FOLLOWERS = USER_BASE + "followers:";  // Set，用户粉丝列表
    public static final String USER_LIKE_ARTICLE = USER_BASE + "like:article:"; // Set，用户点赞的文章
    public static final String USER_COLLECT = USER_BASE + "collect:";    // Set，用户收藏夹
    public static final String USER_ARTICLE_COUNT = USER_BASE + "article:count:"; // String，用户文章数
    public static final String USER_POINTS = USER_BASE + "points:";      // String，用户积分
    
    // 标签相关
    public static final String TAG_BASE = "tag:";
    public static final String TAG_ARTICLE_COUNT = TAG_BASE + "article:count:"; // String，标签文章数
    public static final String TAG_HOT = TAG_BASE + "hot";              // ZSet，热门标签
    
    // 分类相关
    public static final String CATEGORY_BASE = "category:";
    public static final String CATEGORY_TREE = CATEGORY_BASE + "tree";   // String，分类树缓存
    public static final String CATEGORY_ARTICLE = CATEGORY_BASE + "article:"; // List，分类文章列表
    
    // 通知相关
    public static final String NOTIFICATION_BASE = "notification:";
    public static final String NOTIFICATION_UNREAD = NOTIFICATION_BASE + "unread:"; // List，未读通知
    public static final String NOTIFICATION_COUNT = NOTIFICATION_BASE + "count:";   // String，未读通知数
    
    /**
     * 获取文章详情的key
     */
    public static String getArticleDetailKey(Long articleId) {
        return ARTICLE_DETAIL + articleId;
    }
    
    /**
     * 获取文章浏览量的key
     */
    public static String getArticleViewCountKey(Long articleId) {
        return ARTICLE_VIEW_COUNT + articleId;
    }
    
    /**
     * 获取用户信息的key
     */
    public static String getUserInfoKey(Long userId) {
        return USER_INFO + userId;
    }
    
    /**
     * 获取用户关注列表的key
     */
    public static String getUserFollowingKey(Long userId) {
        return USER_FOLLOWING + userId;
    }
    
    /**
     * 获取用户粉丝列表的key
     */
    public static String getUserFollowersKey(Long userId) {
        return USER_FOLLOWERS + userId;
    }
    
    /**
     * 获取分类文章列表的key
     */
    public static String getCategoryArticleKey(Long categoryId) {
        return CATEGORY_ARTICLE + categoryId;
    }
} 