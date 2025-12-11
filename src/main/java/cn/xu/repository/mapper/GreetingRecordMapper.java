package cn.xu.repository.mapper;

import cn.xu.model.entity.GreetingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 打招呼记录Mapper接口
 */
@Mapper
public interface GreetingRecordMapper {
    
    /**
     * 插入打招呼记录
     *
     * @param record 记录
     * @return 影响行数
     */
    int insert(GreetingRecord record);
    
    /**
     * 检查是否存在打招呼记录
     *
     * @param userId 发送者ID
     * @param targetId 接收者ID
     * @return 是否存在
     */
    boolean existsByUserIdAndTargetId(@Param("userId") Long userId, @Param("targetId") Long targetId);
    
    /**
     * 删除打招呼记录（互关后清理）
     *
     * @param userId 用户ID
     * @param targetId 目标用户ID
     * @return 影响行数
     */
    int deleteByUserIdAndTargetId(@Param("userId") Long userId, @Param("targetId") Long targetId);
    
    /**
     * 双向删除打招呼记录（互关时调用）
     *
     * @param userId1 用户1
     * @param userId2 用户2
     * @return 影响行数
     */
    int deleteBidirectional(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
