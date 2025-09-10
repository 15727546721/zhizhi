package cn.xu.domain.like.repository;

import cn.xu.domain.like.model.LikeEntity;
import cn.xu.domain.like.model.LikeType;

/**
 * 点赞仓储接口
 * 遵循DDD原则，只处理点赞领域实体的操作
 */
public interface ILikeRepository {
    /**
     * 保存点赞记录
     */
    void saveLike(LikeEntity likeEntity);

    /**
     * 删除点赞记录
     */
    void remove(Long userId, Long targetId, LikeType type);

    /**
     * 更新点赞状态
     */
    void updateStatus(Long userId, LikeType type, Long targetId, Integer status);

    /**
     * 根据用户ID、类型、目标ID查询点赞记录
     */
    LikeEntity findByUserIdAndTypeAndTargetId(Long userId, LikeType type, Long targetId);

    /**
     * 检查点赞状态
     */
    boolean checkStatus(Long userId, LikeType type, Long targetId);

    /**
     * 增加点赞数
     */
    void incrementLikeCount(Long targetId, LikeType type);

    /**
     * 减少点赞数
     */
    void decrementLikeCount(Long targetId, LikeType type);
}