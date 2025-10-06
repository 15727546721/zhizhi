package cn.xu.domain.user.repository;

import cn.xu.domain.user.model.entity.MemberEntity;
import cn.xu.domain.user.model.entity.PointChangeTypeConfigEntity;
import cn.xu.domain.user.model.entity.PointTransactionEntity;

import java.util.List;

/**
 * 积分流水仓储接口
 * 遵循DDD原则，处理积分流水领域实体的操作
 */
public interface IPointTransactionRepository {
    
    /**
     * 保存积分流水记录
     */
    void savePointTransaction(PointTransactionEntity pointTransactionEntity);
    
    /**
     * 根据用户ID查询积分流水记录
     */
    List<PointTransactionEntity> findPointTransactionsByUserId(Long userId, int page, int size);
    
    /**
     * 根据用户ID和变动类型查询积分流水记录
     */
    List<PointTransactionEntity> findPointTransactionsByUserIdAndType(Long userId, String changeType, int page, int size);
    
    /**
     * 根据用户ID查询今日积分获取记录数
     */
    int countTodayEarnTransactions(Long userId, String changeType);
    
    /**
     * 根据用户ID查询会员信息
     */
    MemberEntity findMemberByUserId(Long userId);
    
    /**
     * 保存会员信息
     */
    void saveMember(MemberEntity memberEntity);
    
    /**
     * 根据变动类型查询配置信息
     */
    PointChangeTypeConfigEntity findPointChangeTypeConfigByType(String changeType);
    
    /**
     * 查询所有启用的积分变动类型配置
     */
    List<PointChangeTypeConfigEntity> findAllActivePointChangeTypeConfigs();
    
    /**
     * 查询积分排行榜
     */
    List<MemberEntity> findPointsRanking(int limit);
    
    /**
     * 查询等级排行榜
     */
    List<MemberEntity> findLevelRanking(int limit);
}