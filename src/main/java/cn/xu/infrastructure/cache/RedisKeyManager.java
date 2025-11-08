package cn.xu.infrastructure.cache;

import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.like.model.LikeType;

import java.util.Arrays;
import java.util.Objects;

/**
 * Redis Key 管理工具类（统一管理所有业务 Redis Key）
 * 命名规范：模块:资源:操作:ID（如有）
 */
public class RedisKeyManager {

    private static final String SEPARATOR = ":";

    /**
     * 拼接 Redis Key，自动使用冒号分隔
     */
    private static String key(Object... parts) {
        return String.join(SEPARATOR, Arrays.stream(parts)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toArray(String[]::new));
    }

    // ===================== 帖子模块 =====================

    public static String postDetailKey(Long postId) {
        return key("post", "detail", postId);
    }

    public static String postViewCountKey(Long postId) {
        return key("post", "view", "count", postId);
    }

    public static String postLikeCountKey(Long postId) {
        return key("post", "like", "count", postId);
    }

    public static String postFavoriteCountKey(Long postId) {
        return key("post", "collect", "count", postId);
    }

    public static String postCommentCountKey(Long postId) {
        return key("post", "comment", "count", postId);
    }

    public static String postHotScoreKey(Long postId) {
        return key("post", "hot", "score", postId);
    }

    public static String postHotRankKey() {
        return key("post", "rank", "hot");
    }

    // 帖子热度缓存key (用于临时存储点赞、收藏、评论等操作对热度的影响)
    public static String postHotCacheKey(Long postId) {
        return key("post", "hot", postId);
    }

    public static String postViewIpKey(Long postId) {
        return key("post", "view", "ip", postId);
    }

    /**
     * 帖子搜索结果缓存Key（基础版本，不包含筛选条件）
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 页面大小
     * @return Redis Key
     */
    public static String postSearchResultKey(String keyword, int page, int size) {
        return postSearchResultKey(keyword, null, page, size);
    }
    
    /**
     * 帖子搜索结果缓存Key（支持筛选条件）
     * @param keyword 搜索关键词
     * @param filterHash 筛选条件哈希值（包含类型、时间范围、排序等）
     * @param page 页码
     * @param size 页面大小
     * @return Redis Key
     */
    public static String postSearchResultKey(String keyword, String filterHash, int page, int size) {
        // 对关键词进行哈希编码，避免key过长和特殊字符问题
        String keywordHash = String.valueOf(keyword.hashCode());
        if (filterHash != null && !filterHash.isEmpty()) {
            return key("post", "search", keywordHash, filterHash, String.valueOf(page), String.valueOf(size));
        }
        return key("post", "search", keywordHash, String.valueOf(page), String.valueOf(size));
    }
    
    /**
     * 生成筛选条件的哈希值
     * @param types 类型列表（已排序）
     * @param timeRange 时间范围（可以是简化格式如"day"或精确格式如"start:2024-01-01T00:00:00|end:2024-01-02T00:00:00"）
     * @param sortOption 排序方式
     * @return 筛选条件哈希值
     */
    public static String generateFilterHash(java.util.List<String> types, String timeRange, String sortOption) {
        StringBuilder sb = new StringBuilder();
        if (types != null && !types.isEmpty()) {
            sb.append("types:").append(String.join(",", types));
        }
        if (timeRange != null && !timeRange.isEmpty() && !"all".equals(timeRange)) {
            if (sb.length() > 0) sb.append("|");
            sb.append("time:").append(timeRange);
        }
        if (sortOption != null && !sortOption.isEmpty() && !"time".equals(sortOption)) {
            if (sb.length() > 0) sb.append("|");
            sb.append("sort:").append(sortOption);
        }
        // 使用MD5或其他哈希算法确保哈希值唯一性和长度固定
        // 这里使用hashCode，对于缓存key来说已经足够
        return sb.length() > 0 ? String.valueOf(sb.toString().hashCode()) : "";
    }

    /**
     * 热门搜索关键词Key（Sorted Set，用于统计热门搜索）
     */
    public static String postSearchHotKeywordsKey() {
        return key("post", "search", "hot", "keywords");
    }

    /**
     * 搜索关键词前缀Key（用于清除相关缓存）
     * @param keyword 搜索关键词
     * @return Redis Key前缀
     */
    public static String postSearchKeyPrefix(String keyword) {
        String keywordHash = String.valueOf(keyword.hashCode());
        return key("post", "search", keywordHash);
    }

    // ===================== 用户模块 =====================

    public static String userInfoKey(Long userId) {
        return key("user", "info", userId);
    }

    public static String userFollowingKey(Long userId) {
        return key("user", "following", userId);
    }

    public static String userFollowersKey(Long userId) {
        return key("user", "followers", userId);
    }

    public static String userLikedPostsKey(Long userId) {
        return key("user", "like", "post", userId);
    }

    public static String userCollectedPostsKey(Long userId) {
        return key("user", "collect", userId);
    }

    public static String userPostCountKey(Long userId) {
        return key("user", "post", "count", userId);
    }

    public static String userPointsKey(Long userId) {
        return key("user", "points", userId);
    }

    /**
     * 用户排行榜Key
     * @param sortType 排序类型：fans(粉丝数)、likes(获赞数)、posts(帖子数)、comprehensive(综合)
     * @return Redis Key
     */
    public static String userRankingKey(String sortType) {
        return key("user", "rank", sortType);
    }

    // ===================== 点赞模块 =====================

    public static String likeRelationKey(LikeType type, Long targetId) {
        return key("like", type.getRedisKeyName(), targetId);
    }

    public static String likeCountKey(LikeType type, Long targetId) {
        return key("like", "count", type.getRedisKeyName(), targetId);
    }

    public static String likeRankKey(LikeType type) {
        return key("like", "rank", type.getRedisKeyName());
    }

    // ===================== 评论模块 =====================

    // 一级评论热点排行 ZSet Key: comment:rank:hot:{type}:{targetId}
    public static String commentHotRankKey(CommentType type, Long targetId) {
        return key("comment", "rank", "hot", type.name().toLowerCase(), targetId);
    }

    // 二级回复热点排行 ZSet Key: comment:rank:hot:{type}:{targetId}:{commentId}
    public static String replyHotRankKey(CommentType type, Long targetId, Long commentId) {
        return key("comment", "rank", "hot", type.name().toLowerCase(), targetId, commentId);
    }

    // 评论数量 Key: comment:count:{targetType}:{targetId}:{commentId}
    public static String commentCountKey(int targetType, Long targetId) {
        return key("comment", "count", targetType, targetId);
    }

    // 评论热度衰减key
    public static String commentHotDecayKey() {
        return key("comment", "hot", "zset");
    }

    // ===================== 分类 & 标签模块 =====================

    public static String tagPostCountKey(Long tagId) {
        return key("tag", "post", "count", tagId);
    }

    public static String tagHotKey() {
        return key("tag", "hot");
    }

    /**
     * 热门标签缓存Key（支持时间维度）
     * @param timeRange 时间范围：today(今日)、week(本周)、month(本月)、all(全部)
     * @param limit 返回数量限制
     * @return Redis Key
     */
    public static String tagHotKey(String timeRange, int limit) {
        return key("tag", "hot", timeRange, String.valueOf(limit));
    }

    public static String categoryTreeKey() {
        return key("category", "tree");
    }

    public static String categoryPostKey(Long categoryId) {
        return key("category", "post", categoryId);
    }

    // ===================== 通知模块 =====================

    public static String notificationUnreadKey(Long userId) {
        return key("notification", "unread", userId);
    }

    public static String notificationCountKey(Long userId) {
        return key("notification", "count", userId);
    }
}