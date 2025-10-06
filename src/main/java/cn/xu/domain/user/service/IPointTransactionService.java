package cn.xu.domain.user.service;

import cn.xu.domain.user.model.entity.MemberEntity;
import cn.xu.domain.user.model.entity.PointTransactionEntity;

import java.util.List;

/**
 * 积分流水领域服务接口
 * 处理积分流水相关的业务逻辑
 */
public interface IPointTransactionService {
    
    /**
     * 增加用户积分
     *
     * @param userId  用户ID
     * @param points  积分数量
     * @param changeType  积分变动类型
     * @param description  积分增加描述
     * @param relatedId  关联ID（可选）
     */
    void addUserPoints(Long userId, Long points, String changeType, String description, Long relatedId);
    
    /**
     * 消费用户积分
     *
     * @param userId  用户ID
     * @param points  积分数量
     * @param changeType  积分变动类型
     * @param description  积分消费描述
     * @param relatedId  关联ID（可选）
     */
    void consumeUserPoints(Long userId, Long points, String changeType, String description, Long relatedId);
    
    /**
     * 获取用户积分流水记录
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 积分流水记录列表
     */
    List<PointTransactionEntity> getUserPointTransactions(Long userId, int page, int size);
    
    /**
     * 获取用户指定类型的积分流水记录
     *
     * @param userId 用户ID
     * @param changeType 变动类型
     * @param page 页码
     * @param size 每页大小
     * @return 积分流水记录列表
     */
    List<PointTransactionEntity> getUserPointTransactionsByType(Long userId, String changeType, int page, int size);
    
    /**
     * 获取用户会员信息
     *
     * @param userId 用户ID
     * @return 会员实体
     */
    MemberEntity getMemberInfo(Long userId);
    
    /**
     * 增加用户经验值
     *
     * @param userId  用户ID
     * @param exp     经验值数量
     * @param reason  经验值增加原因
     */
    void addUserExp(Long userId, Long exp, String reason);
    
    /**
     * 获取积分排行榜
     *
     * @param limit 排行榜数量
     * @return 会员排行榜
     */
    List<MemberEntity> getPointsRanking(int limit);
    
    /**
     * 获取等级排行榜
     *
     * @param limit 排行榜数量
     * @return 会员排行榜
     */
    List<MemberEntity> getLevelRanking(int limit);
    
    /**
     * 冻结用户账户
     *
     * @param userId 用户ID
     */
    void freezeMemberAccount(Long userId);
    
    /**
     * 解冻用户账户
     *
     * @param userId 用户ID
     */
    void unfreezeMemberAccount(Long userId);
}