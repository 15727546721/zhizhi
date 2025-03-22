package cn.xu.domain.like.service;


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
     * @param status   点赞状态
     */
    void like(Long userId, Integer type, Long targetId);

    /**
     * 取消点赞
     *
     * @param userId
     * @param type
     * @param targetId
     */
    void unlike(Long userId, Integer type, Long targetId);

    /**
     * 校验点赞状态
     *
     * @param userId   用户ID
     * @param type     点赞类型
     * @param targetId 被点赞对象ID
     * @return 点赞状态
     */
    boolean checkStatus(Long userId, Integer type, Long targetId);
} 