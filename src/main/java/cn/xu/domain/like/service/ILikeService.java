package cn.xu.domain.like.service;

import cn.xu.domain.like.model.LikeType;

/**
 * 点赞应用服务接口
 */
public interface ILikeService {

    /**
     * 点赞
     *
     * @param userId   用户ID
     * @param type     点赞类型
     * @param targetId 被点赞对象ID
     */
    void like(Long userId, LikeType type, Long targetId);

    /**
     * 取消点赞
     *
     * @param userId
     * @param type
     * @param targetId
     */
    void unlike(Long userId, LikeType type, Long targetId);

    /**
     * 校验点赞状态
     *
     * @param userId   用户ID
     * @param type     点赞类型
     * @param targetId 被点赞对象ID
     * @return 点赞状态
     */
    boolean checkStatus(Long userId, LikeType type, Long targetId);
    
    /**
     * 获取点赞数
     *
     * @param targetId 被点赞对象ID
     * @param type     点赞类型
     * @return 点赞数
     */
    long getLikeCount(Long targetId, LikeType type);
    
    /**
     * 检查并修复指定用户和目标的点赞数据一致性
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    void checkAndRepairLikeConsistency(Long userId, Long targetId, LikeType type);
}