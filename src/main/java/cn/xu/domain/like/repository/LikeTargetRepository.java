package cn.xu.domain.like.repository;

/**
 * 点赞目标仓储接口
 * 用于根据点赞类型更新对应表的点赞数
 * 采用策略模式，消除硬编码的类型判断
 */
public interface LikeTargetRepository {
    
    /**
     * 更新目标的点赞数（增量更新）
     * 
     * @param targetId 目标ID
     * @param increment 增量值，正数表示增加，负数表示减少
     */
    void updateLikeCount(Long targetId, Long increment);
    
    /**
     * 检查是否支持指定的点赞类型
     * 
     * @param likeType 点赞类型
     * @return 是否支持
     */
    boolean supports(cn.xu.domain.like.model.LikeType likeType);
}

