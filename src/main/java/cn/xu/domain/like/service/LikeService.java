package cn.xu.domain.like.service;

import cn.xu.domain.like.command.LikeCommand;

/**
 * 点赞应用服务接口
 */
public interface LikeService {
    
    /**
     * 点赞
     *
     * @param command 点赞命令
     */
    void like(LikeCommand command);

    /**
     * 取消点赞
     *
     * @param command 点赞命令
     */
    void unlike(LikeCommand command);

    /**
     * 获取点赞数量
     *
     * @param targetId 目标ID
     * @param type     点赞类型（大小写不敏感）
     * @return 点赞数量
     */
    Long getLikeCount(Long targetId, String type);

    /**
     * 检查是否已点赞
     *
     * @param userId   用户ID
     * @param targetId 目标ID
     * @param type     点赞类型（大小写不敏感）
     * @return 是否已点赞
     */
    boolean isLiked(Long userId, Long targetId, String type);
} 