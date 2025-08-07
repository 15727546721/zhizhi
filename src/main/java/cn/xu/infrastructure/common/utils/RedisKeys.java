package cn.xu.infrastructure.common.utils;

import cn.xu.domain.like.model.LikeType;

import java.util.Objects;

/**
 * Redis Key 工具类，统一管理业务相关 Redis Key
 */
public class RedisKeys {

    /**
     * 点赞关系 Hash Key
     * 格式：like:{类型}:{目标ID}
     * Hash字段：userId
     * Hash值：点赞状态 ("1" 点赞, "0" 取消)
     */
    public static String likeRelationKey(Integer type, Long targetId) {
        LikeType likeType = Objects.requireNonNull(LikeType.valueOf(type), "点赞类型不能为空");
        switch (likeType) {
            case ARTICLE:
                return "like:article:" + targetId;
            case COMMENT:
                return "like:comment:" + targetId;
            case ESSAY:
                return "like:essay:" + targetId;
            default:
                throw new IllegalArgumentException("不支持的点赞类型: " + type);
        }
    }

    /**
     * 点赞计数 Key
     * 格式：likeCount:{类型}:{目标ID}
     * 存储点赞总数，方便快速读取，不用每次扫描点赞关系
     */
    public static String likeCountKey(Integer type) {
        LikeType likeType = Objects.requireNonNull(LikeType.valueOf(type), "点赞类型不能为空");
        switch (likeType) {
            case ARTICLE:
                return "like:count:article:";
            case COMMENT:
//                return "likeCount:comment:" + targetId;
                return "like:count:comment:";
            case ESSAY:
                return "like:count:essay:";
            default:
                throw new IllegalArgumentException("不支持的点赞类型: " + type);
        }
    }

    /**
     * 点赞排行 Key
     * 格式：likeRank:{类型}:{目标ID}
     * 存储点赞排行，方便快速读取，不用每次扫描点赞关系
     */
    public static String likeRankKey(Integer type) {
        LikeType likeType = Objects.requireNonNull(LikeType.valueOf(type), "点赞类型不能为空");
        switch (likeType) {
            case ARTICLE:
                return "like:rank:article:";
            case COMMENT:
                return "like:rank:comment:";
            case ESSAY:
                return "like:rank:essay:";
            default:
                throw new IllegalArgumentException("不支持的点赞类型: " + type);
        }
    }

    /**
     * 文章热度缓存 Key
     */
    public static String articleHotScoreKey(Long articleId) {
        return "article:hotScore:" + articleId;
    }

    /**
     * 评论热度缓存 Key
     */
    public static String commentHotScoreKey(Long commentId) {
        return "comment:hotScore:" + commentId;
    }

    /**
     * 文章点赞数缓存 Key（可选）
     */
    public static String articleLikeCountKey(Long articleId) {
        return "article:likeCount:" + articleId;
    }

    /**
     * 评论点赞数缓存 Key（可选）
     */
    public static String commentLikeCountKey(Long commentId) {
        return "comment:likeCount:" + commentId;
    }
}
