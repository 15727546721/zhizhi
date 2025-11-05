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