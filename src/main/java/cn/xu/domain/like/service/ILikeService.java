package cn.xu.domain.like.service;


/**
 * 点赞应用服务接口
 */
public interface ILikeService {

    /**
     * 点赞
     * @param userId 用户ID
     * @param type 点赞类型
     * @param targetId 被点赞对象ID
     * @param status 点赞状态
     */
    void like(Long userId, Integer type, Long targetId, Integer status);

} 