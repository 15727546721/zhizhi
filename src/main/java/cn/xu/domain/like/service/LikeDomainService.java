package cn.xu.domain.like.service;

import cn.xu.domain.like.command.LikeCommand;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.like.model.LikeType;

/**
 * 点赞领域服务接口
 */
public interface LikeDomainService {
    /**
     * 处理点赞
     */
    LikeEvent handleLike(LikeCommand command);

    /**
     * 处理取消点赞
     */
    LikeEvent handleUnlike(LikeCommand command);

    /**
     * 获取点赞数量
     */
    Long getLikeCount(Long targetId, LikeType type);

    /**
     * 判断用户是否点赞
     */
    boolean isLiked(Long userId, Long targetId, LikeType type);
} 