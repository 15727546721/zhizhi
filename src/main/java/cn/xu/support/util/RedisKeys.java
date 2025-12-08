package cn.xu.support.util;

import cn.xu.model.entity.Like;
import cn.xu.support.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Redis Key 工具类
 * <p>统一管理业务相关 Redis Key</p>
 */
@Slf4j
public class RedisKeys {

    /**
     * 点赞关系 Hash Key
     * 格式：like:{类型}:{目标ID}
     * Hash字段：userId
     * Hash值：点赞状态("1" 点赞, "0" 取消)
     */
    public static String likeRelationKey(Integer type, Long targetId) {
        Like.LikeType likeType = Objects.requireNonNull(Like.LikeType.valueOf(type), "点赞类型不能为空");
        switch (likeType) {
            case POST:
                return "like:post:" + targetId;
            case COMMENT:
                return "like:comment:" + targetId;
            case ESSAY:
                return "like:essay:" + targetId;
            default:
                log.error("不支持的点赞类型: {}", type);
                throw new BusinessException("不支持的点赞类型: " + type);
        }
    }

    /**
     * 点赞计数 Key
     * 格式：likeCount:{类型}:{目标ID}
     * 存储点赞总数，方便快速读取，不用每次扫描点赞关系
     */
    public static String likeCountKey(Integer type) {
        Like.LikeType likeType = Objects.requireNonNull(Like.LikeType.valueOf(type), "点赞类型不能为空");
        switch (likeType) {
            case POST:
                return "like:count:post:";
            case COMMENT:
//                return "likeCount:comment:" + targetId;
                return "like:count:comment:";
            case ESSAY:
                return "like:count:essay:";
            default:
                log.error("不支持的点赞类型: {}", type);
                throw new BusinessException("不支持的点赞类型: " + type);
        }
    }

    /**
     * 点赞排行 Key
     * 格式：likeRank:{类型}:{目标ID}
     * 存储点赞排行，方便快速读取，不用每次扫描点赞关系
     */
    public static String likeRankKey(Integer type) {
        Like.LikeType likeType = Objects.requireNonNull(Like.LikeType.valueOf(type), "点赞类型不能为空");
        switch (likeType) {
            case POST:
                return "like:rank:post:";
            case COMMENT:
                return "like:rank:comment:";
            case ESSAY:
                return "like:rank:essay:";
            default:
                log.error("不支持的点赞类型: {}", type);
                throw new BusinessException("不支持的点赞类型: " + type);
        }
    }

    /**
     * 帖子热度缓存 Key
     */
    public static String postHotScoreKey(Long postId) {
        return "post:hotScore:" + postId;
    }

    /**
     * 评论热度缓存 Key
     */
    public static String commentHotScoreKey(Long commentId) {
        return "comment:hotScore:" + commentId;
    }

    /**
     * 帖子点赞数缓存 Key（可选）
     */
    public static String postLikeCountKey(Long postId) {
        return "post:likeCount:" + postId;
    }

    /**
     * 评论点赞数缓存 Key（可选）
     */
    public static String commentLikeCountKey(Long commentId) {
        return "comment:likeCount:" + commentId;
    }
}