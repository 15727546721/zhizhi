package cn.xu.domain.like.repository;

import cn.xu.domain.like.model.LikeEntity;
import cn.xu.infrastructure.persistent.po.Like;

/**
 * 点赞仓储接口
 */
public interface ILikeRepository {
    /**
     * 保存点赞记录
     */
    void save(LikeEntity likeEntity);

    Like findByUserIdAndTypeAndTargetId(Long userId, int value, Long targetId);
}