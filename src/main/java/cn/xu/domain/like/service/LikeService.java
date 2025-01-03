package cn.xu.domain.like.service;

import cn.xu.domain.like.model.LikeType;

/**
 * 点赞服务接口
 */
public interface LikeService {
    /**
     * 点赞
     */
    void like(Long userId, Long targetId, LikeType type);

    /**
     * 取消点赞
     */
    void unlike(Long userId, Long targetId, LikeType type);

    /**
     * 获取点赞数量
     */
    Long getLikeCount(Long targetId, LikeType type);

    /**
     * 判断用户是否点赞
     */
    Boolean isLiked(Long userId, Long targetId, LikeType type);
} 