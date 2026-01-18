package cn.xu.repository.mapper;

import cn.xu.model.entity.Column;
import cn.xu.model.entity.ColumnSubscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专栏订阅Mapper接口
 */
@Mapper
public interface ColumnSubscriptionMapper {
    
    /**
     * 插入订阅
     */
    void insert(ColumnSubscription subscription);
    
    /**
     * 更新订阅
     */
    void update(ColumnSubscription subscription);
    
    /**
     * 查询用户对专栏的订阅
     */
    ColumnSubscription selectByUserAndColumn(@Param("userId") Long userId, @Param("columnId") Long columnId);
    
    /**
     * 查询用户订阅的专栏列表
     */
    List<Column> selectSubscribedColumns(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计用户订阅的专栏数
     */
    int countSubscribedColumns(@Param("userId") Long userId);
    
    /**
     * 检查是否已订阅
     */
    int isSubscribed(@Param("userId") Long userId, @Param("columnId") Long columnId);
    
    /**
     * 批量检查订阅状态
     */
    List<Long> batchCheckSubscribed(@Param("userId") Long userId, @Param("columnIds") List<Long> columnIds);
    
    /**
     * 查询用户订阅的专栏ID列表（分页）
     */
    List<Long> selectSubscribedColumnIds(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
}
