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

    /**
     * 更新点赞状态
     */
    void updateStatus(Long userId, Integer type, Long targetId, Integer status);

    /**
     * 根据用户ID、类型、目标ID查询点赞记录
     *
     * @param userId
     * @param value
     * @param targetId
     * @return
     */
    Like findByUserIdAndTypeAndTargetId(Long userId, int value, Long targetId);

    /**
     * 检查点赞状态
     *
     * @param userId
     * @param type
     * @param targetId
     * @return
     */
    boolean checkStatus(Long userId, Integer type, Long targetId);
}