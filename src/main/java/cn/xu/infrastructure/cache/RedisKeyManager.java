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

    /** 拼接 Redis Key，自动使用冒号分隔 */
    private static String key(Object... parts) {
        return String.join(SEPARATOR, Arrays.stream(parts)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toArray(String[]::new));
    }

    // ===================== 文章模块 =====================

    public static String articleDetailKey(Long articleId) {
        return key("article", "detail", articleId);
    }

    public static String articleViewCountKey(Long articleId) {
        return key("article", "view", "count", articleId);
    }

    public static String articleLikeCountKey(Long articleId) {
        return key("article", "like", "count", articleId);
    }

    public static String articleCollectCountKey(Long articleId) {
        return key("article", "collect", "count", articleId);
    }

    public static String articleCommentCountKey(Long articleId) {
        return key("article", "comment", "count", articleId);
    }

    public static String articleHotScoreKey(Long articleId) {
        return key("article", "hot", "score", articleId);
    }

    public static String articleHotRankKey() {
        return key("article", "rank", "hot");
    }

    public static String articleViewIpKey(Long articleId) {
        return key("article", "view", "ip", articleId);
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

    public static String userLikedArticlesKey(Long userId) {
        return key("user", "like", "article", userId);
    }

    public static String userCollectedArticlesKey(Long userId) {
        return key("user", "collect", userId);
    }

    public static String userArticleCountKey(Long userId) {
        return key("user", "article", "count", userId);
    }

    public static String userPointsKey(Long userId) {
        return key("user", "points", userId);
    }

    // ===================== 点赞模块 =====================

    public static String likeRelationKey(LikeType type, Long targetId) {
        return key("like", type.name().toLowerCase(), targetId);
    }

    public static String likeCountKey(LikeType type, Long targetId) {
        return key("like", "count", type.name().toLowerCase(), targetId);
    }

    public static String likeRankKey(LikeType type) {
        return key("like", "rank", type.name().toLowerCase());
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

    // ===================== 分类 & 标签模块 =====================

    public static String tagArticleCountKey(Long tagId) {
        return key("tag", "article", "count", tagId);
    }

    public static String tagHotKey() {
        return key("tag", "hot");
    }

    public static String categoryTreeKey() {
        return key("category", "tree");
    }

    public static String categoryArticleKey(Long categoryId) {
        return key("category", "article", categoryId);
    }

    // ===================== 通知模块 =====================

    public static String notificationUnreadKey(Long userId) {
        return key("notification", "unread", userId);
    }

    public static String notificationCountKey(Long userId) {
        return key("notification", "count", userId);
    }
}
