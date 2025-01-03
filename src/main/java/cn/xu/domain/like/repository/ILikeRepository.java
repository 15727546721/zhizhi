package cn.xu.domain.like.repository;

import cn.xu.domain.like.model.Like;
import cn.xu.domain.like.model.LikeType;

import java.util.Set;

/**
 * 点赞仓储接口
 */
public interface ILikeRepository {
    /**
     * 保存点赞记录
     */
    void save(Like like);

    /**
     * 获取点赞数量
     */
    Long getLikeCount(Long targetId, LikeType type);

    /**
     * 检查是否已点赞
     */
    boolean isLiked(Long userId, Long targetId, LikeType type);

    /**
     * 删除点赞记录
     */
    void delete(Long userId, Long targetId, LikeType type);

    /**
     * 获取点赞用户ID列表
     */
    Set<Long> getLikedUserIds(Long targetId, LikeType type);

    /**
     * 分页获取指定类型的点赞记录
     */
    Set<Like> getPageByType(LikeType type, Integer offset, Integer limit);

    /**
     * 获取指定类型的点赞记录总数
     */
    Long countByType(LikeType type);

    /**
     * 同步数据到缓存
     */
    void syncToCache(Long targetId, LikeType type);

    /**
     * 清理过期缓存
     */
    void cleanExpiredCache();
} 