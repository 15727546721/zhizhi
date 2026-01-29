package cn.xu.repository;

import cn.xu.model.entity.Column;
import cn.xu.model.entity.ColumnSubscription;

import java.util.List;

/**
 * 专栏订阅仓储接口
 */
public interface ColumnSubscriptionRepository {
    
    /**
     * 保存订阅
     */
    void save(ColumnSubscription subscription);
    
    /**
     * 更新订阅
     */
    void update(ColumnSubscription subscription);
    
    /**
     * 查询用户对专栏的订阅
     */
    ColumnSubscription findByUserAndColumn(Long userId, Long columnId);
    
    /**
     * 查询用户订阅的专栏列表
     */
    List<Column> findSubscribedColumns(Long userId, int offset, int limit);
    
    /**
     * 统计用户订阅的专栏数
     */
    int countSubscribedColumns(Long userId);
    
    /**
     * 检查是否已订阅
     */
    boolean isSubscribed(Long userId, Long columnId);
    
    /**
     * 批量检查订阅状态
     */
    List<Long> batchCheckSubscribed(Long userId, List<Long> columnIds);
    
    /**
     * 查询用户订阅的专栏ID列表（分页）
     */
    List<Long> findSubscribedColumnIds(Long userId, int offset, int limit);
}
