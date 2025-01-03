package cn.xu.domain.like.repository;

import cn.xu.domain.like.model.Like;
import cn.xu.domain.like.model.LikeType;

import java.util.Map;

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
     * 判断用户是否点赞
     */
    boolean isLiked(Long userId, Long targetId, LikeType type);

    /**
     * 批量更新点赞数
     */
    void batchUpdateLikeCount(Map<String, Long> likeCounts);

    /**
     * 删除点赞记录
     */
    void delete(Long userId, Long targetId, LikeType type);
} 