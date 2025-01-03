package cn.xu.domain.like.service;

import cn.xu.domain.like.model.LikeType;

/**
 * 点赞领域服务接口
 */
public interface LikeDomainService {
    
    /**
     * 点赞
     *
     * @param userId   用户ID
     * @param targetId 目标ID
     * @param type     点赞类型
     * @return 是否点赞成功
     */
    boolean like(Long userId, Long targetId, LikeType type);

    /**
     * 取消点赞
     *
     * @param userId   用户ID
     * @param targetId 目标ID
     * @param type     点赞类型
     * @return 是否取消成功
     */
    boolean unlike(Long userId, Long targetId, LikeType type);

    /**
     * 获取点赞数量
     *
     * @param targetId 目标ID
     * @param type     点赞类型
     * @return 点赞数量
     */
    Long getLikeCount(Long targetId, LikeType type);

    /**
     * 检查是否已点赞
     *
     * @param userId   用户ID
     * @param targetId 目标ID
     * @param type     点赞类型
     * @return 是否已点赞
     */
    boolean isLiked(Long userId, Long targetId, LikeType type);
} 