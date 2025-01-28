package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.notification.model.entity.NotificationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知DAO接口
 */
@Mapper
public interface INotificationDao {
    
    /**
     * 保存通知
     */
    void insert(NotificationEntity notification);
    
    /**
     * 批量保存通知
     */
    void batchInsert(@Param("notifications") List<NotificationEntity> notifications);
    
    /**
     * 更新通知
     */
    void update(NotificationEntity notification);
    
    /**
     * 根据ID查询通知
     */
    NotificationEntity selectById(@Param("id") Long id);
    
    /**
     * 根据接收者ID和类型分页查询通知
     */
    List<NotificationEntity> findByReceiverIdAndType(
            @Param("receiverId") Long receiverId,
            @Param("type") int type,
            @Param("pageable") org.springframework.data.domain.Pageable pageable);
    
    /**
     * 根据接收者ID和类型分页查询通知（使用offset和limit）
     */
    List<NotificationEntity> selectByReceiverIdAndType(
            @Param("receiverId") Long receiverId,
            @Param("type") int type,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    /**
     * 统计用户未读通知数量
     */
    long countUnreadByReceiverId(@Param("receiverId") Long receiverId);
    
    /**
     * 将通知标记为已读
     */
    void markAsRead(@Param("id") Long id);
    
    /**
     * 将用户的所有通知标记为已读
     */
    void markAllAsRead(@Param("receiverId") Long receiverId);
    
    /**
     * 删除通知
     */
    void deleteById(@Param("id") Long id);
    
    /**
     * 检查通知是否存在
     */
    boolean existsById(@Param("id") Long id);
} 