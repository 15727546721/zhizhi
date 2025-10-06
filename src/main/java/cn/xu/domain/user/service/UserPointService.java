package cn.xu.domain.user.service;

/**
 * 用户积分服务接口
 */
public interface UserPointService {
    /**
     * 增加用户积分
     * @param userId 用户ID
     * @param points 积分数量
     */
    void addUserPoints(Long userId, int points);
    
    /**
     * 减少用户积分
     * @param userId 用户ID
     * @param points 积分数量
     */
    void deductUserPoints(Long userId, int points);
    
    /**
     * 获取用户积分
     * @param userId 用户ID
     * @return 用户积分
     */
    int getUserPoints(Long userId);
}