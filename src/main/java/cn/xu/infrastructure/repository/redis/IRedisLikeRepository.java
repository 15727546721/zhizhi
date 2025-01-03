package cn.xu.infrastructure.repository.redis;

import cn.xu.domain.like.model.LikeType;

import java.util.Set;

/**
 * Redis点赞仓储接口
 */
public interface IRedisLikeRepository {

    /**
     * 保存点赞记录
     */
    void saveLike(Long userId, Long targetId, LikeType type);

    /**
     * 移除点赞记录
     */
    void removeLike(Long userId, Long targetId, LikeType type);

    /**
     * 获取点赞数量
     */
    Long getLikeCount(Long targetId, LikeType type);

    /**
     * 检查是否已点赞
     */
    boolean hasLiked(Long userId, Long targetId, LikeType type);

    /**
     * 获取点赞用户ID列表
     */
    Set<Long> getLikedUserIds(Long targetId, LikeType type);

    /**
     * 清理指定目标的缓存
     */
    void cleanCache(Long targetId, LikeType type);

    /**
     * 批量更新点赞数
     */
    void batchUpdateLikeCount(String key, Long count);
} 