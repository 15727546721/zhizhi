package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.PointTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 积分流水Mapper接口
 */
@Mapper
public interface PointTransactionMapper {
    
    /**
     * 插入积分流水记录
     */
    void insert(PointTransaction pointTransaction);
    
    /**
     * 根据用户ID查询积分流水记录
     */
    List<PointTransaction> findByUserId(@Param("userId") Long userId, 
                                       @Param("offset") int offset, 
                                       @Param("limit") int limit);
    
    /**
     * 根据用户ID和变动类型查询积分流水记录
     */
    List<PointTransaction> findByUserIdAndType(@Param("userId") Long userId, 
                                              @Param("changeType") String changeType,
                                              @Param("offset") int offset, 
                                              @Param("limit") int limit);
    
    /**
     * 统计用户今日某种类型积分获取记录数
     */
    int countTodayEarnTransactions(@Param("userId") Long userId, 
                                  @Param("changeType") String changeType);
}